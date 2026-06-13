import { useState } from 'react';
import { doctorScheduleService } from '../services/api';
import { CalendarDays, ShieldAlert, CheckCircle, Clock } from 'lucide-react';

export default function DoctorLeavesPage() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const currentUser = JSON.parse(localStorage.getItem('med_user'));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const start = new Date(startDate);
    const end = new Date(endDate);
    const today = new Date();
    today.setHours(0,0,0,0);

    if (start < today) {
      setError('Data de început nu poate fi în trecut (cerință @FutureOrPresent).');
      setLoading(false);
      return;
    }
    if (end < start) {
      setError('Data de sfârșit nu poate fi înainte de data de început.');
      setLoading(false);
      return;
    }

    try {
      const payload = {
        doctorId: currentUser?.doctorId || 1,
        startDate: start.toISOString(),
        endDate: end.toISOString()
      };

      const response = await doctorScheduleService.schedulePTO(payload);
      
      setSuccess(response.data || 'Concediu programat cu succes!');
      setStartDate('');
      setEndDate('');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Eroare la programarea concediului.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6 py-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <CalendarDays className="text-indigo-600" />
          Planificator Concediu Doctor
        </h2>
        <p className="text-sm text-gray-500 mt-1">
          Înregistrează perioadele de indisponibilitate (PTO). Sistemul va bloca automat programările pacienților în acest interval.
        </p>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2 animate-fade-in">
          <ShieldAlert size={20} className="shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="p-4 bg-green-50 border-l-4 border-green-500 text-green-700 text-sm rounded-xl flex items-center gap-2 animate-fade-in">
          <CheckCircle size={20} className="shrink-0" />
          <span>{success}</span>
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-2">
                Data Început Concediu
              </label>
              <input 
                type="date"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:outline-none text-sm transition-all"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-2">
                Data Sfârșit Concediu
              </label>
              <input 
                type="date"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:outline-none text-sm transition-all"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="bg-amber-50/60 rounded-xl p-4 flex gap-3 text-amber-800 text-xs leading-relaxed">
            <Clock size={18} className="shrink-0 mt-0.5" />
            <div>
              <span className="font-bold">Notă administrativă:</span> Prin trimiterea acestui formular, vă asumați indisponibilitatea în intervalul selectat. Modificările ulterioare pot fi făcute doar prin contactarea departamentului de HR.
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-bold py-3 px-4 rounded-xl text-sm shadow-sm transition-all text-center flex justify-center items-center"
          >
            {loading ? 'Se procesează...' : 'Programează Concediul'}
          </button>
        </form>
      </div>
    </div>
  );
}