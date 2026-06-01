import { useEffect, useState } from "react";
import { LogOut, Stethoscope, Search, RefreshCw } from "lucide-react";

import api from "../api/axios";
import DoctorCard from "../components/DoctorCard";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Dashboard() {
  const [doctors, setDoctors] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { logout } = useAuth();

  const fetchDoctors = async () => {
    setLoading(true);
    try {
      const response = await api.get("/doctors");
      setDoctors(response.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, []);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const joinQueue = async (doctorId) => {
    try {
      await api.post(`/queue/join?doctorId=${doctorId}`);
      navigate(`/queue?doctorId=${doctorId}`);
    } catch (error) {
      console.error(error);
      if (error.response && error.response.status === 409) {
        navigate(`/queue?doctorId=${doctorId}`);
      } else {
        alert("Failed To Join Queue");
      }
    }
  };

  const filteredDoctors = (doctors || []).filter(
    (doctor) =>
      doctor.specialization &&
      (doctor.name.toLowerCase().includes(search.toLowerCase()) ||
       doctor.specialization.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 text-white">
      {/* Navigation */}
      <nav className="border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl sticky top-0 z-50 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-gradient-to-tr from-indigo-500 to-emerald-500 rounded-xl text-white shadow-md shadow-indigo-500/20">
            <Stethoscope className="w-5 h-5 animate-pulse" />
          </div>
          <span className="font-bold text-xl tracking-tight bg-gradient-to-r from-white to-slate-300 bg-clip-text text-transparent">
            Q-Flow Portal
          </span>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 bg-slate-900 border border-slate-800 hover:border-red-500/50 hover:bg-red-500/10 text-slate-300 hover:text-red-400 font-semibold px-4 py-2 rounded-xl transition-all duration-200 cursor-pointer active:scale-95"
        >
          <LogOut className="w-4 h-4" />
          <span>Logout</span>
        </button>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-10">
        
        {/* Banner Section */}
        <div className="bg-gradient-to-r from-indigo-600/20 via-indigo-900/10 to-slate-900/40 border border-indigo-500/10 rounded-3xl p-8 mb-10 shadow-xl">
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-2">
            Welcome to <span className="bg-gradient-to-r from-indigo-400 to-emerald-400 bg-clip-text text-transparent">Q-Flow Clinic</span>
          </h1>
          <p className="text-slate-400 text-sm sm:text-base max-w-2xl leading-relaxed">
            Select a physician from the list below to join their real-time waiting list. Once you join, you will be able to track your exact position and estimated wait time live.
          </p>
        </div>

        {/* Filter and Search Bar */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 mb-8">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-500" />
            <input
              type="text"
              placeholder="Search by doctor name or speciality..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-slate-900/50 border border-slate-800/80 focus:border-indigo-500 rounded-2xl py-3.5 pl-12 pr-4 text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all duration-200"
            />
          </div>

          <button
            onClick={fetchDoctors}
            className="flex items-center justify-center gap-2 bg-slate-900/50 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 py-3 px-5 rounded-2xl transition-all active:scale-98 cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 text-slate-400 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh list</span>
          </button>
        </div>

        {/* Loading State */}
        {loading && doctors.length === 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((n) => (
              <div key={n} className="bg-slate-900/20 border border-slate-900 rounded-3xl p-6 h-64 animate-pulse flex flex-col justify-between">
                <div>
                  <div className="w-12 h-12 bg-slate-800 rounded-xl mb-4"></div>
                  <div className="w-3/4 h-6 bg-slate-800 rounded-lg mb-2"></div>
                  <div className="w-1/2 h-4 bg-slate-800 rounded-lg"></div>
                </div>
                <div className="w-full h-12 bg-slate-800 rounded-xl"></div>
              </div>
            ))}
          </div>
        ) : filteredDoctors.length === 0 ? (
          <div className="text-center py-20 bg-slate-900/10 border border-slate-900/80 rounded-3xl">
            <Stethoscope className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-slate-300">No Doctors Found</h3>
            <p className="text-slate-500 text-sm mt-1">Try adjusting your search criteria or checking back later.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDoctors.map((doctor) => (
              <DoctorCard key={doctor.id} doctor={doctor} onJoin={joinQueue} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
