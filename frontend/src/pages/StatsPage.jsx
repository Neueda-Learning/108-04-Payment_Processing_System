import Navbar from "../components/Navbar";

function StatsPage() {

  const stats = [
    {
      title: "Total Payments",
      value: "128",
      description: "Payments processed",
    },
    {
      title: "Successful Payments",
      value: "116",
      description: "Completed transactions",
    },
    {
      title: "Failed Payments",
      value: "12",
      description: "Requires attention",
    },
    {
      title: "Total Amount",
      value: "€24,850",
      description: "Processed value",
    },
  ];


  return (

    <div className="min-h-screen relative overflow-hidden">


      {/* Navbar */}
      <div className="relative z-20">
        <Navbar />
      </div>



      {/* Background Image */}
      <div
        className="
          absolute
          inset-0
          bg-cover
          bg-center
          animate-pulse
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85"></div>




      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">



        {/* Header */}
        <div className="max-w-6xl mx-auto mb-8">

          <h2 className="text-2xl sm:text-3xl font-bold text-gray-900">
            Payment Statistics
          </h2>


          <p className="text-gray-600 mt-2 max-w-xl">
            Analyze your payment performance, transaction activity,
            success rates, and processed amounts.
          </p>


        </div>







        {/* Stats Cards */}
        <div
          className="
            max-w-6xl
            mx-auto
            grid
            grid-cols-1
            sm:grid-cols-2
            lg:grid-cols-4
            gap-6
          "
        >

          {stats.map((stat, index) => (

            <div
              key={index}
              className="
                bg-white/95
                backdrop-blur-sm
                border border-gray-200
                rounded-2xl
                shadow-lg
                p-6
                hover:-translate-y-1
                transition
              "
            >

              <h3 className="text-sm text-gray-500">
                {stat.title}
              </h3>


              <p className="text-3xl font-bold text-gray-900 mt-3">
                {stat.value}
              </p>


              <p className="text-sm text-gray-600 mt-2">
                {stat.description}
              </p>


            </div>

          ))}


        </div>







        {/* Analytics Section */}
        <div
          className="
            max-w-6xl
            mx-auto
            mt-8
            bg-white/95
            backdrop-blur-sm
            border border-gray-200
            rounded-2xl
            shadow-lg
            p-6 sm:p-8
          "
        >


          <h3 className="text-xl font-semibold text-gray-900 mb-4">
            Payment Analytics
          </h3>



          <div
            className="
              h-48
              flex
              items-center
              justify-center
              border-2
              border-dashed
              border-gray-200
              rounded-xl
            "
          >

            <p className="text-gray-400 text-center">
              Charts and payment trends will appear here
            </p>


          </div>



        </div>







        {/* Info Banner */}
        <div
          className="
            max-w-6xl
            mx-auto
            mt-6
            bg-white/90
            backdrop-blur-sm
            border border-gray-200
            rounded-xl
            shadow-md
            p-5
          "
        >

          <p className="text-gray-700 text-sm">
            Payment analytics helps you understand transaction
            performance, monitor success rates, and identify trends
            for better financial decisions.
          </p>


        </div>



      </div>


    </div>

  );
}

export default StatsPage;