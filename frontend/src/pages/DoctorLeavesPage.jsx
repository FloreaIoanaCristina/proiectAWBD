import { useState, useEffect, useCallback, useMemo } from 'react';
import { doctorScheduleService } from '../services/api';
import { CalendarDays, ShieldAlert, CheckCircle, Clock, Calendar as CalendarIcon } from 'lucide-react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';

export default function DoctorLeavesPage() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [leaveEvents, setLeaveEvents] = useState([]);

  const currentUser = useMemo(() => {
    const savedUser = localStorage.getItem('med_user');
    return savedUser ? JSON.parse(savedUser) : null;
  }, []);

  const doctorId = currentUser?.doctorId || currentUser?.id || 1;

  const fetchMyLeaves = useCallback(async () => {
    try {
      const response = await doctorScheduleService.getByDoctorId(doctorId);
      
        const formattedEvents = (response.data || []).map(entry => {

            return {
            id: entry.id, 
            title: 'CONCEDIU (PTO)',
            start: entry.from,
            end: entry.to,
            backgroundColor: '#ef4444', 
            borderColor: '#dc2626',
            allDay: true
            };
        });
      
      setLeaveEvents(formattedEvents);
    } catch (err) {
      console.error('Nu s-au putut încărca concediile pentru calendar:', err);
    }
  }, [doctorId]);

  useEffect(() => {
    if (doctorId) {
      fetchMyLeaves();
    }
  }, [fetchMyLeaves, doctorId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const today = new Date();
    today.setHours(0,0,0,0);

    const start = new Date(startDate);
    const end = new Date(endDate);

    if (start < today) {
      setError('Data de început nu poate fi în trecut.');
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
        doctorId: doctorId,
        startDate: startDate, 
        endDate: endDate
      };

      const response = await doctorScheduleService.schedulePTO(payload);
      
      setSuccess(typeof response.data === 'string' ? response.data : 'Concediu programat cu succes!');
      setStartDate('');
      setEndDate('');

      fetchMyLeaves();
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Eroare la programarea concediului.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 py-4">
      <div>
        <h2 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <CalendarDays className="text-indigo-600" />
          Planificator Concedii & Indisponibilitate
        </h2>
        <p className="text-sm text-gray-500 mt-1">
          Înregistrează perioadele de PTO. Sistemul va bloca automat programările pacienților în acest interval.
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 h-fit space-y-5">
          <h3 className="font-bold text-gray-800 text-base flex items-center gap-2 border-b pb-3 border-gray-100">
            <CalendarIcon size={18} className="text-indigo-500" />
            Solicitare Perioadă Nouă
          </h3>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-2">
                Data Început
              </label>
              <input 
                type="date"
                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:outline-none text-sm transition-all bg-white shadow-sm"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-2">
                Data Sfârșit
              </label>
              <input 
                type="date"
                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:outline-none text-sm transition-all bg-white shadow-sm"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
              />
            </div>

            <div className="bg-amber-50/70 rounded-xl p-4 flex gap-3 text-amber-800 text-xs leading-relaxed border border-amber-100">
              <Clock size={18} className="shrink-0 mt-0.5 text-amber-600" />
              <div>
                <span className="font-bold">Notă:</span> Prin trimiterea formularului, intervalul va deveni indisponibil instantaneu pentru pacienți.
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-bold py-3 px-4 rounded-xl text-sm shadow-sm transition-all text-center flex justify-center items-center gap-2"
            >
              {loading ? 'Se procesează...' : 'Blochează Perioada'}
            </button>
          </form>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 lg:col-span-2">
          <div className="mb-4">
            <h3 className="font-bold text-gray-800 text-base">Vizualizare Agendă Concedii</h3>
            <p className="text-xs text-gray-400">Zilele marcate cu roșu reprezintă perioadele tale de indisponibilitate active.</p>
          </div>
          
          <div className="calendar-container text-sm">
            <FullCalendar
              plugins={[dayGridPlugin]}
              initialView="dayGridMonth"
              locale="ro"
              events={leaveEvents}
              headerToolbar={{
                left: 'prev,next today',
                center: 'title',
                right: ''
              }}
              height="auto"
              buttonText={{
                today: 'Azi'
              }}
            />
          </div>
        </div>

      </div>
    </div>
  );
}