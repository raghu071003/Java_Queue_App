import { Clock, Stethoscope, ArrowRight } from "lucide-react";

export default function DoctorCard({ doctor, onJoin }) {
  return (
    <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800/80 rounded-2xl p-6 transition-all duration-300 hover:border-indigo-500/50 hover:bg-slate-900/60 hover:-translate-y-1 hover:shadow-xl hover:shadow-indigo-500/5 flex flex-col justify-between h-full">
      <div>
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="p-3 bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 rounded-xl">
            <Stethoscope className="w-6 h-6" />
          </div>
          <span className="text-xs font-semibold px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-full">
            Available
          </span>
        </div>

        <h3 className="text-xl font-bold text-white mb-1 group-hover:text-indigo-400 transition-colors">
          {doctor.name}
        </h3>
        <p className="text-sm text-slate-400 font-medium mb-4">
          {doctor.specialization}
        </p>

        <div className="flex items-center gap-2 text-slate-400 text-xs bg-slate-950/30 rounded-xl p-3 border border-slate-900 mb-6">
          <Clock className="w-4 h-4 text-slate-500" />
          <span>Avg. Service Time: <strong className="text-slate-200">{doctor.avgServiceTime} mins</strong> per patient</span>
        </div>
      </div>

      <button
        onClick={() => onJoin(doctor.id)}
        className="w-full bg-slate-950 border border-slate-800 hover:border-indigo-500 hover:bg-indigo-500 hover:text-white text-slate-300 font-semibold py-3 rounded-xl transition-all duration-200 flex items-center justify-center gap-2 group cursor-pointer active:scale-[0.98]"
      >
        <span>Join Waitlist</span>
        <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
      </button>
    </div>
  );
}