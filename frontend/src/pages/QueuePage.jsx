import { useEffect, useState } from "react";
import { ArrowLeft, Clock, User, CheckCircle2, AlertCircle, Sparkles, LogOut } from "lucide-react";
import { useNavigate, useSearchParams } from "react-router-dom";

import api from "../api/axios";
import { connectQueueSocket } from "../services/websocket";
import { useAuth } from "../context/AuthContext";

export default function QueuePage() {
  const [searchParams] = useSearchParams();
  const doctorIdParam = searchParams.get("doctorId");
  const doctorId = doctorIdParam ? parseInt(doctorIdParam, 10) : null;

  const [queue, setQueue] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { logout } = useAuth();

  const loadQueue = async () => {
    try {
      const url = doctorId ? `/queue/my-position?doctorId=${doctorId}` : "/queue/my-position";
      const response = await api.get(url);
      setQueue(response.data.data);
      setError("");
    } catch (err) {
      console.error(err);
      setError("You are not currently in any active queue.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQueue();

    const client = connectQueueSocket(doctorId || 1, (updatedQueue) => {
      console.log("Realtime Update", updatedQueue);
      loadQueue();
    });

    return () => {
      client.deactivate();
    };
  }, [doctorId]);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 border-4 border-indigo-500/20 border-t-indigo-500 rounded-full animate-spin"></div>
          <span className="text-slate-400 font-semibold text-sm">Retrieving your queue position...</span>
        </div>
      </div>
    );
  }

  if (error || !queue) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 flex items-center justify-center p-4">
        <div className="w-full max-w-md bg-slate-900/60 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 text-center">
          <AlertCircle className="w-16 h-16 text-indigo-400 mx-auto mb-6 animate-bounce" />
          <h2 className="text-2xl font-bold text-white mb-2">No Active Queue</h2>
          <p className="text-slate-400 text-sm mb-6 leading-relaxed">
            {error || "It looks like you aren't currently waiting in a doctor's queue. Head back to the dashboard to join one."}
          </p>
          <button
            onClick={() => navigate("/dashboard")}
            className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 rounded-xl transition active:scale-98 cursor-pointer"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const getStatusColor = (status) => {
    switch (status) {
      case "IN_PROGRESS":
        return "from-blue-500 to-indigo-500 text-blue-400 border-blue-500/30";
      case "DONE":
        return "from-emerald-500 to-teal-500 text-emerald-400 border-emerald-500/30";
      case "WAITING":
      default:
        return "from-amber-500 to-orange-500 text-amber-400 border-amber-500/30";
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 text-white">
      {/* Header */}
      <header className="border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-xl sticky top-0 z-50 px-6 py-4 flex items-center justify-between">
        <button
          onClick={() => navigate("/dashboard")}
          className="flex items-center gap-2 bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 font-semibold px-4 py-2 rounded-xl transition active:scale-95 cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Dashboard</span>
        </button>
        <span className="font-bold text-lg hidden sm:block">Queue Tracker</span>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 bg-slate-900 border border-slate-800 hover:border-red-500/50 hover:bg-red-500/10 text-slate-300 hover:text-red-400 font-semibold px-4 py-2 rounded-xl transition cursor-pointer active:scale-95"
        >
          <LogOut className="w-4 h-4" />
          <span>Logout</span>
        </button>
      </header>

      {/* Content */}
      <main className="max-w-4xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          
          {/* Main Status Panel */}
          <div className="md:col-span-2 space-y-6">
            <div className="bg-slate-900/60 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 relative overflow-hidden shadow-2xl">
              
              {/* Heartbeat Pulse Indicator */}
              <div className="absolute top-6 right-6 flex items-center gap-2 bg-emerald-500/10 border border-emerald-500/30 px-3 py-1.5 rounded-full">
                <span className="w-2.5 h-2.5 bg-emerald-400 rounded-full animate-ping"></span>
                <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">Live tracking</span>
              </div>

              <h2 className="text-slate-400 font-semibold uppercase tracking-wider text-xs mb-8">Current Position</h2>
              
              <div className="flex items-baseline gap-4 mb-4">
                <span className="text-8xl font-black tracking-tighter bg-gradient-to-br from-white via-slate-200 to-slate-500 bg-clip-text text-transparent">
                  {queue.position}
                </span>
                <span className="text-slate-500 text-lg font-semibold">in line</span>
              </div>

              {queue.status === "IN_PROGRESS" && (
                <div className="flex items-center gap-2 bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 rounded-2xl p-4 mb-6 text-sm">
                  <Sparkles className="w-5 h-5 text-indigo-400 shrink-0" />
                  <span>The doctor is ready for you! Please proceed to the consultation room.</span>
                </div>
              )}

              {/* Status Badge */}
              <div className="mt-8 pt-8 border-t border-slate-800 flex items-center justify-between">
                <div>
                  <span className="text-xs text-slate-500 block mb-1">Doctor</span>
                  <span className="font-bold text-white text-lg">{queue.doctorName}</span>
                </div>
                <span className={`px-4 py-1.5 rounded-full border text-xs font-bold uppercase tracking-wider bg-slate-950/60 ${getStatusColor(queue.status).split(" ").slice(2).join(" ")}`}>
                  {queue.status.replace("_", " ")}
                </span>
              </div>
            </div>

            {/* Stepper Timeline */}
            <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-8 shadow-xl">
              <h3 className="font-bold text-lg text-white mb-6">Consultation Progress</h3>
              
              <div className="relative space-y-8 pl-8 before:absolute before:left-3.5 before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-800">
                
                {/* Step 1 */}
                <div className="relative flex items-start gap-4">
                  <div className="absolute -left-8 w-7 h-7 rounded-full bg-emerald-500/10 border-2 border-emerald-500 flex items-center justify-center text-emerald-400 z-10">
                    <CheckCircle2 className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="font-bold text-white text-sm">Waiting in line</h4>
                    <p className="text-slate-400 text-xs mt-1">You joined the waitlist successfully.</p>
                  </div>
                </div>

                {/* Step 2 */}
                <div className="relative flex items-start gap-4">
                  <div className={`absolute -left-8 w-7 h-7 rounded-full flex items-center justify-center z-10 transition-all ${
                    queue.status === "IN_PROGRESS" || queue.status === "DONE"
                      ? "bg-indigo-500/10 border-2 border-indigo-500 text-indigo-400"
                      : "bg-slate-950 border-2 border-slate-800 text-slate-600"
                  }`}>
                    {queue.status === "DONE" ? <CheckCircle2 className="w-4 h-4" /> : <span className="text-xs font-bold">02</span>}
                  </div>
                  <div>
                    <h4 className={`font-bold text-sm ${queue.status === "IN_PROGRESS" ? "text-indigo-400" : queue.status === "DONE" ? "text-white" : "text-slate-500"}`}>
                      Consultation Active
                    </h4>
                    <p className="text-slate-400 text-xs mt-1">The doctor is reviewing your details.</p>
                  </div>
                </div>

                {/* Step 3 */}
                <div className="relative flex items-start gap-4">
                  <div className={`absolute -left-8 w-7 h-7 rounded-full flex items-center justify-center z-10 transition-all ${
                    queue.status === "DONE"
                      ? "bg-emerald-500/10 border-2 border-emerald-500 text-emerald-400"
                      : "bg-slate-950 border-2 border-slate-800 text-slate-600"
                  }`}>
                    <span className="text-xs font-bold">03</span>
                  </div>
                  <div>
                    <h4 className={`font-bold text-sm ${queue.status === "DONE" ? "text-emerald-400" : "text-slate-500"}`}>Completed</h4>
                    <p className="text-slate-400 text-xs mt-1">Your session is finished. Have a healthy day!</p>
                  </div>
                </div>

              </div>
            </div>
          </div>

          {/* Side Info Cards */}
          <div className="space-y-6">
            
            {/* Wait Card */}
            <div className="bg-slate-900/60 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col items-center text-center">
              <div className="p-3 bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 rounded-2xl mb-4">
                <Clock className="w-6 h-6 animate-pulse" />
              </div>
              <span className="text-xs text-slate-500 uppercase tracking-wider font-semibold block mb-1">Estimated Wait</span>
              <span className="text-3xl font-extrabold text-white mb-2">{queue.estimatedWaitTime} mins</span>
              <p className="text-slate-500 text-xs leading-relaxed">
                Calculated in real-time based on people ahead of you and the physician's average service speed.
              </p>
            </div>

            {/* Instruction Card */}
            <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
              <h4 className="font-bold text-white text-sm">Quick Instructions</h4>
              <ul className="text-xs text-slate-400 space-y-3 leading-relaxed">
                <li className="flex gap-2">
                  <span className="text-indigo-400 font-bold">•</span>
                  <span>Keep this tab open; your position will automatically decrement as people are treated.</span>
                </li>
                <li className="flex gap-2">
                  <span className="text-indigo-400 font-bold">•</span>
                  <span>A chime or visual alert will prompt you when your status changes to <strong>In Progress</strong>.</span>
                </li>
              </ul>
            </div>

          </div>

        </div>
      </main>
    </div>
  );
}