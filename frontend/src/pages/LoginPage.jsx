import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

function LoginPage() {

  const [account, setAccount] = useState("");
  const [showCreate, setShowCreate] = useState(false);

  const [newAccount, setNewAccount] = useState({
    accountNumber: "",
    accountHolderName: "",
    balance: "",
    accountCurrencyType: "INR",
    status: "ACTIVE"

    
  });

  const navigate = useNavigate();


 const handleLogin = async () => {

  if (!account) {
    alert("Please enter account number");
    return;
  }

  try {

    await axios.get(`http://localhost:8080/accounts/${account}`);

    localStorage.setItem("account", account);
    navigate("/home");

  } catch (error) {

    if (error.response && error.response.status === 404) {
      alert("Account not found");
    } else {
      alert("Something went wrong");
    }

  }

};


  const handleCreateAccount = async () => {

    try {
//console.log(newAccount);

      const response = await axios.post(
        "http://localhost:8080/accounts/",
        newAccount
      );

      //console.log("Account created:", response.data);

      alert("Account created successfully");

      setShowCreate(false);

    } catch (error) {

      console.error(error);
      alert("Account creation failed");

    }

  };


  return (

    <div className="min-h-screen relative overflow-hidden flex items-center justify-center px-4">


      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1600&q=80')"
        }}
      />


      <div className="absolute inset-0 bg-white/85"></div>


      <div className="relative z-10 bg-white/95 w-full max-w-md rounded-2xl shadow-xl p-8">


        <div className="text-center mb-8">

          <h1 className="text-3xl font-bold">
            FlashPay
          </h1>

          <p className="text-gray-500 mt-2">
            Fast • Secure • Instant Payments
          </p>

        </div>



        {/* Login Section */}

        <h2 className="text-xl font-semibold mb-4">
          Account Login
        </h2>


        <input
          type="text"
          placeholder="Enter account number"
          value={account}
          onChange={(e)=>setAccount(e.target.value)}
          className="w-full px-4 py-3 border rounded-lg"
        />


        <button
          onClick={handleLogin}
          className="w-full mt-5 bg-red-600 text-white py-3 rounded-lg"
        >
          Login
        </button>



        <div className="text-center my-5 text-gray-400">
          OR
        </div>



        {/* Create Account Button */}

        <button
          onClick={()=>setShowCreate(true)}
          className="w-full border border-red-600 text-red-600 py-3 rounded-lg"
        >
          Create New Account
        </button>



      </div>



      {/* Create Account Popup */}

      {showCreate && (

        <div className="
          fixed inset-0 
          bg-black/40 
          flex items-center 
          justify-center
          z-50
        ">


          <div className="
            bg-white
            rounded-xl
            p-6
            w-full
            max-w-md
          ">


            <h2 className="text-xl font-bold mb-5">
              Create Account
            </h2>



            {[
              ["accountNumber","Account Number"],
              ["accountHolderName","Account Holder Name"],
              ["balance","Initial Balance"]
            ].map(([key,label])=>(

              <input
                key={key}
                placeholder={label}
                value={newAccount[key]}
                onChange={(e)=>
                  setNewAccount({
                    ...newAccount,
                    [key]:e.target.value
                  })
                }
                className="
                  w-full
                  mb-3
                  px-4
                  py-2
                  border
                  rounded
                "
              />

            ))}



            <select
              className="w-full mb-3 px-4 py-2 border rounded"
              value={newAccount.accountCurrencyType}
              onChange={(e)=>
                setNewAccount({
                  ...newAccount,
                  accountCurrencyType:e.target.value
                })
              }
            >

              <option>INR</option>
              <option>USD</option>
              <option>EUR</option>
              <option>GBP</option>
              <option>JPY</option>
              <option>AUD</option>
              <option>CAD</option>
              <option>CHF</option>
              <option>CNY</option>
              <option>MXN</option>

            </select>



            <button
              onClick={handleCreateAccount}
              className="
                w-full
                bg-red-600
                text-white
                py-3
                rounded
                mb-3
              "
            >
              Create Account
            </button>



            <button
              onClick={()=>setShowCreate(false)}
              className="
                w-full
                border
                py-3
                rounded
              "
            >
              Cancel
            </button>


          </div>


        </div>

      )}



    </div>

  );
}


export default LoginPage;