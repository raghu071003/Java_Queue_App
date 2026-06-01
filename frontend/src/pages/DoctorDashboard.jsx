import { useEffect, useState } from "react";
import { Stethoscope, User, Play, CheckCircle2, Users, Clock, LogOut, RefreshCw } from "lucide-react";
import { useNavigate } from "react-router-dom";

import api from "../api/axios";
import { connectQueueSocket } from "../services/websocket";
import { useAuth } from "../context/AuthContext";

export default function DoctorDashboard() {
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState(null);

  const [waitingQueue, setWaitingQueue] = useState([]);
  const [activePatient, setActivePatient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  
  // Onboarding Profile states
  const [profilePending, setProfilePending] = useState(false);
  const [specialityInput, setSpecialityInput] = useState("");
  const [serviceTimeInput, setServiceTimeInput] = useState("10");

  const navigate = useNavigate();
  const { logout, user, updateUser } = useAuth();

  // 1. Fetch Doctor List on Mount
  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const response = await api.get("/doctors");
        const allDocs = response.data.data || [];
        
        if (user && user.role === "DOCTOR" && user.doctorId) {
          const myDoc = allDocs.filter(d => d.id === user.doctorId);
          if (myDoc.length > 0) {
            setDoctors(myDoc);
            setSelectedDoctorId(myDoc[0].id);
            // If specialization is missing, trigger profile completion modal
            if (!myDoc[0].specialization || !myDoc[0].avgServiceTime) {
              setProfilePending(true);
            }
          } else {
            const docInfo = { id: user.doctorId, name: user.name, specialization: null, avgServiceTime: null };
            setDoctors([docInfo]);
            setSelectedDoctorId(user.doctorId);
            setProfilePending(true);
          }
        } else if (user && user.role === "DOCTOR" && !user.doctorId) {
          setProfilePending(true);
        } else {
          setDoctors(allDocs);
          if (allDocs.length > 0) {
            setSelectedDoctorId(allDocs[0].id);
          } else {
            setLoading(false);
          }
        }
      } catch (err) {
        console.error(err);
        setLoading(false);
      }
    };
    fetchDoctors();
  }, [user]);

  // 2. Fetch Active Consultation and Waiting Queue
  const loadData = async (id) => {
    if (!id) return;
    try {
      const activeResponse = await api.get(`/queue/active/${id}`);
      setActivePatient(activeResponse.data.data);

      const queueResponse = await api.get(`/queue/doctor/${id}`);
      setWaitingQueue(queueResponse.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 3. Establish WebSocket connection whenever the selected doctor changes
  useEffect(() => {
    if (!selectedDoctorId) return;

    setLoading(true);
    loadData(selectedDoctorId);

    const client = connectQueueSocket(selectedDoctorId, (updatedQueue) => {
      console.log("WebSocket Update Received for Doctor", updatedQueue);
      loadData(selectedDoctorId);
    });

    return () => {
      client.deactivate();
    };
  }, [selectedDoctorId]);

  const handleStartConsultation = async () => {
    if ((waitingQueue || []).length === 0 || !selectedDoctorId) return;
    setActionLoading(true);
    try {
      const response = await api.post(`/queue/start?doctorId=${selectedDoctorId}`);
      setActivePatient(response.data.data);
      
      const queueResponse = await api.get(`/queue/doctor/${selectedDoctorId}`);
      setWaitingQueue(queueResponse.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setActionLoading(false);
    }
  };

  const handleCompleteConsultation = async () => {
    if (!activePatient || !selectedDoctorId) return;
    setActionLoading(true);
    try {
      await api.post(`/queue/complete/${activePatient.id}`);
      setActivePatient(null);
      
      const queueResponse = await api.get(`/queue/doctor/${selectedDoctorId}`);
      setWaitingQueue(queueResponse.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setActionLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  if (profilePending) {
    const handleProfileSubmit = async (e) => {
      e.preventDefault();
      if (!specialityInput || !serviceTimeInput) {
        alert("Please enter all details.");
        return;
      }
      try {
        setActionLoading(true);
        const response = await api.post("/doctors", {
          name: user.name,
          email: user.email,
          specialization: specialityInput,
          avgServiceTime: parseInt(serviceTimeInput, 10)
        });
        
        // Update context with verified doctorId
        const updatedUser = { ...user, doctorId: response.data.data.id };
        updateUser(updatedUser);
        setProfilePending(false);
        // Refresh workspace list
        window.location.reload();
      } catch (err) {
        console.error(err);
        alert("Failed to complete profile.");
      } finally {
        setActionLoading(false);
      }
    };

    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 flex items-center justify-center p-4">
        <div className="w-full max-w-md bg-slate-900/60 backdrop-blur-xl border border-slate-800/80 rounded-3xl shadow-2xl p-8 text-white">
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center p-3 bg-gradient-to-tr from-indigo-500 to-emerald-500 rounded-2xl shadow-lg mb-4 text-white shadow-indigo-500/30">
              <Stethoscope className="w-8 h-8 animate-pulse" />
            </div>
            <h1 className="text-2xl font-extrabold tracking-tight bg-gradient-to-r from-white to-slate-300 bg-clip-text text-transparent animate-pulse">
              Complete Physician Profile
            </h1>
            <p className="text-xs text-slate-400 mt-2 leading-relaxed">
              Dr. {user.name}, please complete your onboarding details. This registers your clinic queue so patients can discover and join your line live.
            </p>
          </div>

          <form onSubmit={handleProfileSubmit} className="space-y-5">
            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">
                Medical Speciality
              </label>
              <input
                type="text"
                placeholder="Cardiologist, General Physician, etc."
                value={specialityInput}
                onChange={(e) => setSpecialityInput(e.target.value)}
                className="w-full bg-slate-950/50 border border-slate-800 focus:border-indigo-500 rounded-xl py-3 px-4 text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all duration-200"
              />
            </div>

            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">
                Average Service Time (minutes)
              </label>
              <input
                type="number"
                placeholder="10"
                min="1"
                value={serviceTimeInput}
                onChange={(e) => setServiceTimeInput(e.target.value)}
                className="w-full bg-slate-950/50 border border-slate-800 focus:border-indigo-500 rounded-xl py-3 px-4 text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all duration-200"
              />
            </div>

            <button
              type="submit"
              disabled={actionLoading}
              className="w-full bg-gradient-to-r from-indigo-500 to-emerald-500 hover:from-indigo-600 hover:to-emerald-600 disabled:from-slate-700 disabled:to-slate-800 text-white font-semibold py-3.5 rounded-xl shadow-lg shadow-indigo-500/20 active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2 cursor-pointer mt-2"
            >
              {actionLoading ? (
                <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              ) : (
                "Activate Clinic Workspace"
              )}
            </button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 text-white">
      {/* Navbar */}
      <nav className="border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl sticky top-0 z-50 px-6 py-4 flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-2.5 flex-wrap">
          <div className="p-2 bg-gradient-to-tr from-indigo-500 to-emerald-500 rounded-xl text-white shadow-md shadow-indigo-500/20">
            <Stethoscope className="w-5 h-5 animate-pulse" />
          </div>
          <span className="font-bold text-xl tracking-tight bg-gradient-to-r from-white to-slate-300 bg-clip-text text-transparent">
            Q-Flow Portal
          </span>
          <span className="bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs px-2.5 py-1 rounded-lg ml-2 font-semibold">
            Doctor Workspace
          </span>

          {/* Switcher Dropdown */}
          {user && user.role !== "DOCTOR" && (doctors || []).length > 0 && (
            <div className="flex items-center gap-2 sm:ml-6 ml-0">
              <span className="text-xs text-slate-500 uppercase tracking-wider font-semibold">Workspace:</span>
              <select
                value={selectedDoctorId || ""}
                onChange={(e) => setSelectedDoctorId(parseInt(e.target.value, 10))}
                className="bg-slate-900 border border-slate-800 focus:border-indigo-500 text-white text-xs font-semibold py-1.5 px-3 rounded-lg focus:outline-none focus:ring-1 focus:ring-indigo-500 cursor-pointer"
              >
                {(doctors || []).map((doc) => (
                  <option key={doc.id} value={doc.id}>
                    {doc.name} ({doc.specialization})
                  </option>
                ))}
              </select>
            </div>
          )}
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
        
        {/* Banner */}
        <div className="bg-gradient-to-r from-indigo-600/20 via-indigo-900/10 to-slate-900/40 border border-indigo-500/10 rounded-3xl p-8 mb-10 shadow-xl flex items-center justify-between gap-6 flex-wrap">
          <div>
            <h1 className="text-3xl font-extrabold tracking-tight mb-2">
              Physician Consultation Dashboard
            </h1>
            <p className="text-slate-400 text-sm max-w-xl leading-relaxed">
              Manage your real-time waiting line, pull the next waiting patient into your office, and complete sessions dynamically. Your patients' queue details will sync instantly.
            </p>
          </div>
          <button
            onClick={() => loadData(selectedDoctorId)}
            disabled={!selectedDoctorId}
            className="flex items-center gap-2 bg-slate-900/50 hover:bg-slate-900 border border-slate-800 py-3 px-5 rounded-2xl transition cursor-pointer active:scale-98"
          >
            <RefreshCw className={`w-4 h-4 text-slate-400 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh Workspace</span>
          </button>
        </div>

        {/* Loading / No Doctor State */}
        {loading && doctors.length === 0 ? (
          <div className="text-center py-20 bg-slate-900/10 border border-slate-900/80 rounded-3xl animate-pulse">
            <Stethoscope className="w-12 h-12 text-indigo-400/50 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-slate-300">Loading workspaces...</h3>
          </div>
        ) : doctors.length === 0 ? (
          <div className="text-center py-20 bg-slate-900/10 border border-slate-900/80 rounded-3xl">
            <Stethoscope className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-slate-300">No Doctor Workspaces Configured</h3>
            <p className="text-slate-500 text-sm mt-1">Please register doctors in the database to active workspaces.</p>
          </div>
        ) : (
          /* Dashboard Grid */
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            {/* Active Patient Column */}
            <div className="lg:col-span-2 space-y-6">
              <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 shadow-2xl relative min-h-[350px] flex flex-col justify-between overflow-hidden">
                
                {/* Pulse Indicator */}
                <div className="absolute top-6 right-6 flex items-center gap-2 bg-indigo-500/10 border border-indigo-500/30 px-3 py-1.5 rounded-full">
                  <span className="w-2.5 h-2.5 bg-indigo-400 rounded-full animate-pulse"></span>
                  <span className="text-xs font-semibold text-indigo-400 uppercase tracking-wider">Active Consultation</span>
                </div>

                <h2 className="text-slate-400 font-semibold uppercase tracking-wider text-xs mb-6">Patient Treatment Room</h2>

                {activePatient ? (
                  <div className="flex-1 flex flex-col justify-between">
                    <div className="space-y-6 mt-4">
                      <div className="flex items-center gap-4">
                        <div className="w-16 h-16 bg-gradient-to-tr from-indigo-500 to-emerald-500 text-white rounded-2xl flex items-center justify-center font-extrabold text-2xl shadow-lg shadow-indigo-500/20">
                          {activePatient.patientName.charAt(0)}
                        </div>
                        <div>
                          <h3 className="text-2xl font-black text-white">{activePatient.patientName}</h3>
                          <p className="text-slate-400 text-sm">{activePatient.patientEmail}</p>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4 mt-6">
                        <div className="bg-slate-950/40 border border-slate-900 rounded-2xl p-4">
                          <span className="text-xs text-slate-500 block mb-1">Joined Line At</span>
                          <span className="font-semibold text-slate-200">
                            {new Date(activePatient.joinedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        <div className="bg-slate-950/40 border border-slate-900 rounded-2xl p-4">
                          <span className="text-xs text-slate-500 block mb-1">Session Token</span>
                          <span className="font-mono font-bold text-slate-200">#{activePatient.id}</span>
                        </div>
                      </div>
                    </div>

                    <button
                      onClick={handleCompleteConsultation}
                      disabled={actionLoading}
                      className="w-full bg-gradient-to-r from-red-500 to-rose-600 hover:from-red-600 hover:to-rose-700 disabled:from-slate-700 disabled:to-slate-800 text-white font-semibold py-4 rounded-2xl shadow-xl shadow-red-500/10 transition duration-200 flex items-center justify-center gap-2 cursor-pointer mt-8"
                    >
                      {actionLoading ? (
                        <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                      ) : (
                        <>
                          <CheckCircle2 className="w-5 h-5" />
                          <span>Complete Session & Dismiss</span>
                        </>
                      )}
                    </button>
                  </div>
                ) : (
                  <div className="flex-1 flex flex-col justify-between items-center text-center mt-6">
                    <div className="my-auto py-8">
                      <User className="w-16 h-16 text-slate-700 mx-auto mb-4" />
                      <h3 className="text-xl font-bold text-slate-300">Office is Currently Empty</h3>
                      <p className="text-slate-500 text-sm max-w-sm mt-1 leading-relaxed">
                        Click the button below to treat the next waiting patient in line.
                      </p>
                    </div>

                    <button
                      onClick={handleStartConsultation}
                      disabled={actionLoading || waitingQueue.length === 0}
                      className="w-full bg-gradient-to-r from-indigo-500 to-emerald-500 hover:from-indigo-600 hover:to-emerald-600 disabled:from-slate-800 disabled:to-slate-900 disabled:border-slate-800/80 disabled:text-slate-600 text-white font-semibold py-4 rounded-2xl shadow-xl shadow-indigo-500/10 transition duration-200 flex items-center justify-center gap-2 cursor-pointer active:scale-98"
                  >
                    {actionLoading ? (
                      <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    ) : (
                      <>
                        <Play className="w-5 h-5" />
                        <span>Call Next Patient ({waitingQueue.length} waiting)</span>
                      </>
                    )}
                    </button>
                  </div>
                )}

              </div>
            </div>

            {/* Waiting Queue List Column */}
            <div className="space-y-6">
              <div className="bg-slate-900/60 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col h-[530px]">
                
                <div className="flex items-center justify-between border-b border-slate-800/60 pb-4 mb-4">
                  <div className="flex items-center gap-2">
                    <Users className="w-5 h-5 text-slate-400" />
                    <h3 className="font-bold text-white">Live Waiting Line</h3>
                  </div>
                  <span className="text-xs font-semibold px-2.5 py-1 bg-slate-950 text-indigo-400 rounded-lg border border-slate-900">
                    {waitingQueue.length} Active
                  </span>
                </div>

                <div className="flex-1 overflow-y-auto space-y-3 pr-2 scrollbar-thin scrollbar-thumb-slate-800">
                  {waitingQueue.length === 0 ? (
                    <div className="text-center my-auto py-16">
                      <CheckCircle2 className="w-12 h-12 text-slate-700 mx-auto mb-3" />
                      <h4 className="font-bold text-slate-400 text-sm">Line is Empty</h4>
                      <p className="text-slate-600 text-xs mt-1">No patients are currently waiting.</p>
                    </div>
                  ) : (
                    waitingQueue.map((patient, index) => (
                      <div
                        key={patient.position}
                        className="bg-slate-950/40 border border-slate-900/60 hover:border-slate-800 rounded-2xl p-4 flex items-center justify-between gap-4 transition-all"
                      >
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 bg-slate-900 border border-slate-800 text-indigo-400 font-bold text-sm rounded-xl flex items-center justify-center">
                            {index + 1}
                          </div>
                          <div>
                            <h4 className="font-bold text-slate-200 text-sm">{patient.patientName}</h4>
                            <span className="text-[10px] text-slate-500 uppercase tracking-wider">WAITING</span>
                          </div>
                        </div>
                        
                        <div className="flex items-center gap-1.5 text-slate-500 text-xs bg-slate-950 px-2.5 py-1 rounded-lg">
                          <Clock className="w-3.5 h-3.5" />
                          <span>#{patient.position}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>

              </div>
            </div>

          </div>
        )}
      </main>
    </div>
  );
}