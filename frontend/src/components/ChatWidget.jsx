import { useEffect, useRef, useState } from "react";

// Global FAQ + Analytics chatbot launcher. Mounted once in App.jsx (outside the
// route Switch) so it is reachable from every page, not just the home screen.
function ChatWidget() {
  const [chatOpen, setChatOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (chatOpen) {
      messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages, loading, chatOpen]);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    const question = input;
    setMessages((prev) => [...prev, { role: "user", text: question }]);
    setInput("");
    setLoading(true);

    try {
      const response = await fetch(`${import.meta.env.VITE_CHATBOT_URL}/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: question }),
      });

      if (!response.ok) throw new Error("Assistant request failed");

      const data = await response.json();
      setMessages((prev) => [...prev, { role: "bot", text: data.reply, source: data.source }]);
    } catch (error) {
      setMessages((prev) => [
        ...prev,
        { role: "bot", text: "Sorry, I couldn't reach the assistant. Please try again in a moment.", error: true },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Launcher */}
      <button
        onClick={() => setChatOpen((o) => !o)}
        aria-label={chatOpen ? "Close FlashPay Assistant" : "Open FlashPay Assistant"}
        aria-expanded={chatOpen}
        className="fixed bottom-6 right-6 z-50 flex h-14 w-14 items-center justify-center rounded-full bg-red-600 text-white text-2xl shadow-xl shadow-red-600/30 transition hover:bg-red-700 hover:scale-105 active:scale-95 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500 cursor-pointer"
      >
        {chatOpen ? "✕" : "💬"}
      </button>

      {/* Chat window */}
      {chatOpen && (
        <div
          role="dialog"
          aria-label="FlashPay Assistant"
          className="fixed bottom-24 right-6 z-50 flex h-[28rem] w-[calc(100vw-3rem)] max-w-sm flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-2xl dark:border-gray-800 dark:bg-gray-900"
        >
          {/* Header */}
          <div className="flex items-center justify-between bg-red-600 px-4 py-3">
            <div>
              <p className="font-semibold text-white text-sm">FlashPay Assistant</p>
              <p className="text-red-200 text-xs">FAQ &amp; live analytics</p>
            </div>
            <button
              onClick={() => setChatOpen(false)}
              aria-label="Close chat"
              className="rounded-full p-1 text-white/80 hover:bg-white/10 hover:text-white focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white cursor-pointer"
            >
              ✕
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 space-y-3 overflow-y-auto p-4">
            {messages.length === 0 && !loading && (
              <div className="flex h-full flex-col items-center justify-center text-center px-4">
                <p className="text-3xl">👋</p>
                <p className="mt-2 text-sm font-medium text-gray-700 dark:text-gray-200">Ask me anything about FlashPay</p>
                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">FAQs, payment status, or analytics like "how many payments failed today?"</p>
              </div>
            )}

            {messages.map((msg, index) => (
              <div key={index} className={msg.role === "user" ? "text-right" : "text-left"}>
                {msg.role === "bot" && msg.source && (
                  <span className="mb-1 inline-block rounded-full bg-gray-100 dark:bg-gray-800 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                    {msg.source.startsWith("stats") ? "Live data" : "FAQ"}
                  </span>
                )}
                <br />
                <span
                  className={`inline-block max-w-[85%] rounded-lg px-3 py-2 text-sm ${
                    msg.role === "user"
                      ? "bg-red-100 text-gray-900 dark:bg-red-500/20 dark:text-gray-100"
                      : msg.error
                      ? "bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400"
                      : "bg-gray-100 text-gray-900 dark:bg-gray-800 dark:text-gray-100"
                  }`}
                >
                  {msg.text}
                </span>
              </div>
            ))}

            {loading && (
              <div className="flex items-center gap-1.5 px-1 py-1" aria-live="polite">
                <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-gray-400 dark:bg-gray-500 [animation-delay:-0.3s]" />
                <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-gray-400 dark:bg-gray-500 [animation-delay:-0.15s]" />
                <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-gray-400 dark:bg-gray-500" />
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="flex items-center gap-2 border-t border-gray-200 dark:border-gray-800 p-2">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") sendMessage();
              }}
              placeholder="Ask about payments..."
              aria-label="Message"
              className="flex-1 rounded-lg bg-gray-50 dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 placeholder:text-gray-400 dark:placeholder:text-gray-500 outline-none focus-visible:ring-2 focus-visible:ring-red-500"
            />
            <button
              onClick={sendMessage}
              disabled={!input.trim() || loading}
              aria-label="Send message"
              className="rounded-lg bg-red-600 px-3 py-2 text-sm font-medium text-white transition hover:bg-red-700 disabled:opacity-40 disabled:cursor-not-allowed focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500 cursor-pointer"
            >
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export default ChatWidget;
