import { useState } from "react";
import Navbar from "../components/Navbar";

function FAQPage() {

  const [openIndex, setOpenIndex] = useState(null);


  const faqs = [
    {
      question: "How fast are payments processed?",
      answer:
        "FlashPay processes payments instantly and keeps you updated through every payment status change."
    },
    {
      question: "How can I track my payment?",
      answer:
        "You can view all your transactions and payment status updates from the Payment History section."
    },
    {
      question: "What happens if my payment fails?",
      answer:
        "Failed payments will show the reason and error details so you can take the required action."
    },
    {
      question: "Can I make payments to any account?",
      answer:
        "You can make payments to supported accounts through the secure FlashPay payment system."
    }
  ];



  return (

    <div className="min-h-screen relative overflow-hidden bg-gray-50 dark:bg-gray-950">


      {/* Navbar */}
      <div className="relative z-50">
        <Navbar />
      </div>




      {/* Background Image */}
      <div
        className="
          absolute
          inset-0
          -z-10
          bg-cover
          bg-center
          animate-pulse
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 -z-10 bg-white/85 dark:bg-gray-950/90"></div>





      {/* Content */}
      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">


        {/* Header */}
        <div className="max-w-4xl mx-auto mb-8">

          <h2 className="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-gray-100">
            Frequently Asked Questions
          </h2>


          <p className="text-gray-600 dark:text-gray-400 mt-2">
            Find quick answers about payments, transactions,
            security, and FlashPay services.
          </p>


        </div>






        {/* FAQ Accordion */}
        <div className="max-w-4xl mx-auto space-y-4">


          {faqs.map((faq, index) => (

            <div
              key={index}
              className="
                bg-white/95
                dark:bg-gray-900/95
                backdrop-blur-sm
                border border-gray-200 dark:border-gray-800
                rounded-xl
                shadow-lg
                overflow-hidden
              "
            >


              {/* Question */}
              <button
                onClick={() =>
                  setOpenIndex(openIndex === index ? null : index)
                }
                className="
                  w-full
                  flex
                  justify-between
                  items-center
                  text-left
                  p-6
                  hover:bg-gray-50 dark:hover:bg-gray-800/60
                  transition
                "
              >

                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {faq.question}
                </h3>


                <span
                  className="
                    text-xl
                    text-red-600
                    font-bold
                  "
                >
                  {openIndex === index ? "−" : "+"}
                </span>


              </button>





              {/* Answer Dropdown */}
              <div
                className={`
                  transition-all
                  duration-300
                  overflow-hidden
                  ${
                    openIndex === index
                      ? "max-h-40 opacity-100"
                      : "max-h-0 opacity-0"
                  }
                `}
              >

                <p
                  className="
                    px-6
                    pb-6
                    text-gray-600 dark:text-gray-400
                    leading-relaxed
                  "
                >
                  {faq.answer}
                </p>


              </div>



            </div>

          ))}


        </div>








        {/* Support Section */}
        <div
          className="
            max-w-4xl
            mx-auto
            mt-8
            bg-white/90
            backdrop-blur-sm
            border border-gray-200
            rounded-xl
            shadow-md
            p-6
            flex
            flex-col
            sm:flex-row
            items-start
            sm:items-center
            justify-between
            gap-4
          "
        >

          <div>

            <h3 className="font-semibold text-gray-900">
              Need more help?
            </h3>


            <p className="text-gray-600 text-sm mt-1">
              Contact support for assistance with payments,
              transactions, or account-related questions.
            </p>


          </div>



          <button
            className="
              bg-red-600
              text-white
              px-5
              py-2
              rounded-lg
              hover:bg-red-700
              transition
              w-full
              sm:w-auto
            "
          >
            Contact Support
          </button>


        </div>



      </div>


    </div>

  );
}


export default FAQPage;