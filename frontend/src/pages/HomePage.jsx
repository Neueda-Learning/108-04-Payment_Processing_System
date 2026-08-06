import { useNavigate } from "react-router-dom";
import { useState } from "react";
import Navbar from "../components/Navbar";

function HomePage() {

  const navigate = useNavigate();


  // ================= CHATBOT STATE =================

  const [chatOpen, setChatOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);



  const sendMessage = async () => {

    if (!input.trim()) return;


    const question = input;


    setMessages((prev) => [
      ...prev,
      {
        role: "user",
        text: question
      }
    ]);


    setInput("");
    setLoading(true);



    try {

      const response = await fetch(
        `${import.meta.env.VITE_CHATBOT_URL}/chat`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            message: question
          })
        }
      );


      const data = await response.json();



      setMessages((prev)=>[
        ...prev,
        {
          role:"bot",
          text:data.reply
        }
      ]);



    } catch(error){


      setMessages((prev)=>[
        ...prev,
        {
          role:"bot",
          text:"Unable to connect to assistant."
        }
      ]);


    }


    setLoading(false);

  };




  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex flex-col relative">


      {/* Navbar */}

      <div className="relative z-20">
        <Navbar />
      </div>




      {/* Background Image */}

      <div
        className="absolute inset-0 bg-cover bg-center dark:opacity-20"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1600&q=80')",
        }}
      ></div>



      {/* Overlay */}

      <div className="absolute inset-0 bg-white/80 dark:bg-gray-950/90"></div>




      {/* Main Content */}

      <div className="relative flex-1 z-10">



        {/* Hero */}

        <div className="max-w-5xl mx-auto px-6 pt-20">


          <div className="max-w-2xl">


            <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 leading-tight">
              Fast and Secure Payments
            </h1>


            <p className="text-gray-600 dark:text-gray-400 mt-4 text-lg">
              Send money instantly with a simple and reliable payment system.
              Track transactions, view history, and manage everything in one place.
            </p>



            <button

              onClick={() => navigate("/payments")}

              className="
                mt-8
                px-6
                py-3
                bg-red-600
                text-white
                rounded-md
                font-medium
                hover:bg-red-700
                transition
                cursor-pointer
                shadow-sm
              "

            >

              Make Payment

            </button>


          </div>


        </div>





        {/* Cards */}

        <div className="max-w-6xl mx-auto px-6 mt-28 pb-20">


          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">


            <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2">

              <h3 className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                Instant Transfers
              </h3>

              <p className="text-gray-600 dark:text-gray-400 mt-3">
                Payments are processed in real-time with minimal delay,
                ensuring fast transactions.
              </p>

            </div>





            <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2 delay-100">

              <h3 className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                Secure System
              </h3>

              <p className="text-gray-600 dark:text-gray-400 mt-3">
                Built with secure backend processing and transaction validation.
              </p>

            </div>






            <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2 delay-200">

              <h3 className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                Full Visibility
              </h3>

              <p className="text-gray-600 dark:text-gray-400 mt-3">
                Monitor your transactions with complete history and analytics.
              </p>

            </div>



          </div>


        </div>


      </div>







      {/* ================= CHATBOT BUTTON ================= */}



      <button

        onClick={()=>setChatOpen(true)}

        className="
          fixed
          bottom-6
          right-6
          z-50
          w-16
          h-16
          rounded-full
          bg-red-600
          text-white
          text-3xl
          shadow-xl
          hover:bg-red-700
        "

      >

        💬

      </button>







      {/* ================= CHAT WINDOW ================= */}



      {chatOpen && (


        <div

          className="
            fixed
            bottom-24
            right-6
            z-50
            w-96
            bg-white
            dark:bg-gray-900
            rounded-xl
            shadow-2xl
            border
            dark:border-gray-800
            overflow-hidden
          "

        >




          {/* Header */}

          <div

            className="
              bg-red-600
              text-white
              p-4
              flex
              justify-between
            "

          >

            <span className="font-semibold">
              FlashPay Assistant
            </span>


            <button
              onClick={()=>setChatOpen(false)}
            >
              ✕
            </button>


          </div>






          {/* Messages */}


          <div

            className="
              h-80
              overflow-y-auto
              p-4
              space-y-3
            "

          >


            {messages.map((msg,index)=>(


              <div

                key={index}

                className={
                  msg.role==="user"
                  ?
                  "text-right"
                  :
                  "text-left"
                }

              >


                <span

                  className={`
                    inline-block
                    px-3
                    py-2
                    rounded-lg
                    ${
                      msg.role==="user"
                      ?
                      "bg-red-100 dark:bg-red-500/20 text-gray-900 dark:text-gray-100"
                      :
                      "bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    }
                  `}

                >

                  {msg.text}

                </span>


              </div>


            ))}



            {loading && (

              <p className="text-gray-400 dark:text-gray-500">
                Typing...
              </p>

            )}


          </div>






          {/* Input */}


          <div className="flex border-t dark:border-gray-800">


            <input

              value={input}

              onChange={(e)=>setInput(e.target.value)}

              onKeyDown={(e)=>{
                if(e.key==="Enter")
                  sendMessage();
              }}

              placeholder="Ask about payments..."

              className="
                flex-1
                px-4
                py-3
                outline-none
                bg-white
                dark:bg-gray-900
                text-gray-900
                dark:text-gray-100
                placeholder:text-gray-400
                dark:placeholder:text-gray-500
              "

            />



            <button

              onClick={sendMessage}

              className="
                bg-red-600
                text-white
                px-5
              "

            >

              Send

            </button>



          </div>



        </div>


      )}




    </div>
  );
}

export default HomePage;