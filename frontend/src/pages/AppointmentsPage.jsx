import { useState, useEffect, useCallback, useMemo } from 'react';
import { appointmentService, medicalServiceService, patientService } from '../services/api';
import { Calendar, Clock, User, Trash2, Edit2, PlusCircle, AlertCircle, ArrowUpDown, Star } from 'lucide-react';

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState([]);
  const [medicalServices, setMedicalServices] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [filteredDoctors, setFilteredDoctors] = useState([]);
  
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState('appointmentFrom');
  const [sortDir, setSortDir] = useState('asc');

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [feedbackRating, setFeedbackRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);

  const [selectedAppointment, setSelectedAppointment] = useState(null);
  
  const [patientId, setPatientId] = useState('');
  const [medicalServiceId, setMedicalServiceId] = useState('');

  const [selectedDate, setSelectedDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState('');

  const [editDate, setEditDate] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [patientNamePreview, setPatientNamePreview] = useState('');
  const [isSearchingPatient, setIsSearchingPatient] = useState(false);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);

  const currentUser = useMemo(() => JSON.parse(localStorage.getItem('med_user')), []);
  const isPatient = currentUser?.role === 'PATIENT';

  const fetchAppointments = useCallback(async () => {
    if (!currentUser) return;

    const { role, profileId } = currentUser;
    const params = {
      page: page,
      size: size,
      sort: `${sortBy},${sortDir}`
    };

    try {
      let response;

      if (currentUser.role === 'DOCTOR') {
        response = await appointmentService.getByDoctorId(profileId, params);
      } else {
        response = await appointmentService.getByPatientId(profileId, params);
      }
      setAppointments(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Nu s-au putut încărca programările.');
    }
  }, [page, size, sortBy, sortDir, currentUser]);

  const fetchServices = async () => {
    try {
      const res = await medicalServiceService.getAll();
      setMedicalServices(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchAppointments();
    fetchServices();
  }, [fetchAppointments]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0); 
  };

  useEffect(() => {
    if (!patientId || currentUser.role === 'PATIENT') {
      setPatientNamePreview('');
      return;
    }

    setIsSearchingPatient(true);
    const delayDebounceFn = setTimeout(async () => {
      try {
        const response = await patientService.getById(patientId); 
        if (response.data) {
          setPatientNamePreview(`Pacient identificat: ${response.data.name}`);
        }
      } catch (err) {
        setPatientNamePreview('Nu s-a găsit niciun pacient cu acest ID.');
      } finally {
        setIsSearchingPatient(false);
      }
    }, 800);

    return () => clearTimeout(delayDebounceFn);
  }, [patientId, currentUser.role]);

  useEffect(() => {
  if (!medicalServiceId) {
    setFilteredDoctors([]);
    setDoctorId('');
    return;
  }

  const selectedService = medicalServices.find(s => s.id === parseInt(medicalServiceId));

    if (selectedService && selectedService.medicalServiceDoctors) {
      setFilteredDoctors(selectedService.medicalServiceDoctors);
    } else {
      setFilteredDoctors([]);
    }
    
    setDoctorId('');
    setSelectedDate('');
    setAvailableSlots([]);
    setSelectedSlot('');
  }, [medicalServiceId, medicalServices]);

  useEffect(() => {
    if (!medicalServiceId || !selectedDate) {
      setAvailableSlots([]);
      setSelectedSlot('');
      return;
    }

    const loadSlots = async () => {
      setIsLoadingSlots(true);
      setError('');
      try {
        const response = await appointmentService.getAvailableSlots(
          medicalServiceId,
          selectedDate,
          doctorId || null
        );
        setAvailableSlots(response.data || []);
      } catch (err) {
        setError('Nu s-au putut prelua intervalele orare pentru data selectată.');
      } finally {
        setIsLoadingSlots(false);
      }
    };

    loadSlots();
  }, [medicalServiceId, doctorId, selectedDate]);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const isUserPatient = currentUser.role === 'PATIENT';

    if (!isUserPatient && !patientId) {
      setError('ID-ul pacientului este obligatoriu.');
      return;
    }

    if (!medicalServiceId || !selectedDate || !selectedSlot) {
      setError('Toate câmpurile, inclusiv selectarea unui interval orar, sunt obligatorii.');
      return;
    }

    try {
      const payload = {
        patientId: isUserPatient ?  null : parseInt(patientId),
        medicalServiceId: parseInt(medicalServiceId),
        doctorId: doctorId ? parseInt(doctorId) : null,
        appointmentFrom: selectedSlot
      };

      const response = await appointmentService.create(payload);
      setSuccess('Programarea a fost creată cu succes!');
      setShowCreateModal(false);
      setPatientId('');
      setMedicalServiceId('');
      setDoctorId('');
      setSelectedDate('');
      setSelectedSlot('');
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la crearea programării.');
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!editDate) {
      setError('Noua dată și oră este obligatorie.');
      return;
    }
    if (new Date(editDate) < new Date()) {
      setError('Nu puteți muta o programare în trecut.');
      return;
    }

    try {
      const isoDate = new Date(editDate).toISOString();
      await appointmentService.update(selectedAppointment.id, isoDate);
      
      setSuccess('Programarea a fost mutată cu succes!');
      setShowEditModal(false);
      setSelectedSlot('');
      setSelectedDate('');
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data?.message || 'Intervalul orar selectat nu este disponibil.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Sigur doriți să anulați definitiv această programare?')) return;
    setError('');
    setSuccess('');

    try {
      await appointmentService.delete(id);
      setSuccess('Programarea a fost anulată cu succes.');
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data?.message || 'Nu s-a putut anula programarea.');
    }
  };

  const handleFeedbackSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      await appointmentService.submitFeedback(selectedAppointment.id, feedbackRating);
      setSuccess(`Feedback-ul pentru programarea #${selectedAppointment.id} a fost trimis cu succes!`);
      setShowFeedbackModal(false);
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data || 'Eroare la trimiterea feedback-ului.');
    }
  };

  const canSubmitFeedback = (app) => {
    if (!isPatient) return false;
    if (app.status?.toLowerCase() === 'completed') return false;

    const appointmentStartTime = new Date(app.appointmentFrom).getTime();
    const thirtyMinutesInMs = 30 * 60 * 1000;
    const now = new Date().getTime();

    return now > (appointmentStartTime + thirtyMinutesInMs);
  };

  const formatTimeLabel = (isoString) => {
    return new Date(isoString).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Gestiune Programări</h2>
          <p className="text-sm text-gray-500">Vizualizează, sortează și planifică consultațiile clinicii</p>
        </div>
        
        <button 
          onClick={() => setShowCreateModal(true)}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors"
        >
          <PlusCircle size={18} />
          Programare Nouă
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2">
          <AlertCircle size={20} className="shrink-0" />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="p-4 bg-green-50 border-l-4 border-green-500 text-green-700 text-sm rounded-xl">
          {success}
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
              <th className="py-4 px-6">ID</th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('patient.name')}>
                <div className="flex items-center gap-1">Pacient <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6">Serviciu Medical</th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('appointmentFrom')}>
                <div className="flex items-center gap-1">Data & Ora <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6 text-center">Status / Evaluare</th>
              <th className="py-4 px-6 text-center">Acțiuni</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
            {appointments.length > 0 ? (
              appointments.map((app) => (
                <tr key={app.id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="py-4 px-6 font-semibold text-gray-400">#{app.id}</td>
                  <td className="py-4 px-6">
                    <div className="flex items-center gap-2">
                      <User size={16} className="text-gray-400" />
                      <span className="font-medium text-gray-900">{app.patient?.name || 'Nespecificat'}</span>
                    </div>
                  </td>
                  <td className="py-4 px-6 text-gray-500">{app.medicalService?.name}</td>
                  <td className="py-4 px-6">
                    <div className="flex flex-col">
                      <span className="flex items-center gap-1 font-medium text-gray-800">
                        <Calendar size={14} className="text-indigo-500" />
                        {new Date(app.appointmentFrom).toLocaleDateString('ro-RO')}
                      </span>
                      <span className="flex items-center gap-1 text-xs text-gray-400 mt-0.5">
                        <Clock size={14} />
                        {formatTimeLabel(app.appointmentFrom)}
                      </span>
                    </div>
                  </td>
                  
                  <td className="py-4 px-6 text-center">
                    {app.status?.toLowerCase() === 'completed' ? (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800">
                        Finalizată / Evaluată
                      </span>
                    ) : canSubmitFeedback(app) ? (
                      <button
                        onClick={() => {
                          setSelectedAppointment(app);
                          setFeedbackRating(5);
                          setShowFeedbackModal(true);
                        }}
                        className="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold px-3 py-1.5 rounded-xl"
                      >
                        Acordă Feedback
                      </button>
                    ) : (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">
                        Programată
                      </span>
                    )}
                  </td>

                  <td className="py-4 px-6">
                    <div className="flex justify-center items-center gap-3">
                      {app.status?.toLowerCase() !== 'completed' && (
                        <>
                          <button 
                            onClick={() => {
                              setSelectedAppointment(app);
                              setEditDate(app.appointmentFrom.substring(0, 16)); 
                              setShowEditModal(true);
                            }}
                            className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Reschedule / Reprogramare"
                          >
                            <Edit2 size={16} />
                          </button>
                          <button 
                            onClick={() => handleDelete(app.id)}
                            className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Anulează Programarea"
                          >
                            <Trash2 size={16} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="py-8 text-center text-gray-400 bg-gray-50/20">
                  Nu există programări înregistrate pentru acest cont sau această pagină.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {totalPages > 1 && (
          <div className="bg-gray-50 px-6 py-4 border-t border-gray-100 flex items-center justify-between text-sm">
            <span className="text-gray-500">
              Pagina <strong className="text-gray-700">{page + 1}</strong> din <strong className="text-gray-700">{totalPages}</strong>
            </span>
            <div className="flex gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 font-medium transition-colors"
              >
                Anterior
              </button>
              <button
                disabled={page === totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 font-medium transition-colors"
              >
                Următor
              </button>
            </div>
          </div>
        )}
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Planifică o Programare Nouă</h3>
            <form onSubmit={handleCreateSubmit} className="space-y-4">
              {!isPatient && (
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">ID Pacient</label>
                  <input 
                    type="number"
                    className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    placeholder="Introduceți ID-ul numeric al pacientului"
                    value={patientId}
                    onChange={(e) => setPatientId(e.target.value)}
                    required
                  />
                  {patientId && (
                    <p className="text-xs mt-1 font-semibold">
                      { isSearchingPatient ? 'Se caută în baza de date...' : patientNamePreview }
                    </p>
                  )}
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Serviciu Medical Solicitat</label>
                <select 
                  className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  value={medicalServiceId}
                  onChange={(e) => setMedicalServiceId(e.target.value)}
                  required
                >
                  <option value="">Selectați un serviciu...</option>
                  {medicalServices.map(srv => (
                    <option key={srv.id} value={srv.id}>{srv.name} ({srv.specialization})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Medic Disponibil</label>
                <select 
                  className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-400"
                  value={doctorId}
                  onChange={(e) => setDoctorId(e.target.value)}
                  disabled={!medicalServiceId || filteredDoctors.length === 0}
                  required
                >
                  {!medicalServiceId ? (
                    <option value="">Selectați mai întâi un serviciu...</option>
                  ) : filteredDoctors.length === 0 ? (
                    <option value="">Serviciu fără alocare de medic obligatoriu</option>
                  ) : (
                    <>
                      <option value="">Alegeți medicul...</option>
                      {filteredDoctors.map(doc => (
                        <option key={doc.id} value={doc.id}>Dr. {doc.name}</option>
                      ))}
                    </>
                  )}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Alege Ziua</label>
                <input 
                  type="date"
                  min={new Date().toISOString().split('T')[0]} 
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  value={selectedDate}
                  onChange={(e) => { setSelectedDate(e.target.value); setSelectedSlot(''); }}
                  disabled={!medicalServiceId}
                  required
                />
              </div>

              {selectedDate && (
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-2 flex items-center gap-1">
                    <Clock size={14} /> Ore disponibile pentru data selectată
                  </label>
                  {isLoadingSlots ? (
                    <p className="text-xs text-gray-400 animate-pulse">Se calculează disponibilitatea sloturilor...</p>
                  ) : availableSlots.length === 0 ? (
                    <p className="text-xs text-red-500 font-medium bg-red-50 p-2.5 rounded-lg">
                      Ne pare rău, nu există ore libere sau medicul este în concediu pe această dată.
                    </p>
                  ) : (
                    <div className="grid grid-cols-4 gap-2">
                      {availableSlots.map((slotIso) => {
                        const isSelected = selectedSlot === slotIso;
                        return (
                          <button
                            key={slotIso}
                            type="button"
                            onClick={() => setSelectedSlot(slotIso)}
                            className={`py-2 px-1 text-xs font-semibold rounded-lg border text-center transition-all ${
                              isSelected 
                                ? 'bg-indigo-600 border-indigo-600 text-white shadow-sm ring-2 ring-indigo-300' 
                                : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50 hover:border-gray-300'
                            }`}
                          >
                            {formatTimeLabel(slotIso)}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <button 
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium transition-colors"
                >
                  Anulare
                </button>
                <button 
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold transition-colors"
                >
                  Confirmă Planificarea
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showEditModal && selectedAppointment && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Reprogramare Consultație</h3>
            <p className="text-xs text-gray-400 mb-4">Modificați data pentru programarea #{selectedAppointment.id} ({selectedAppointment.patient?.name})</p>
            
            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Noua Dată și Oră</label>
                <input 
                  type="datetime-local"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  value={editDate}
                  onChange={(e) => setEditDate(e.target.value)}
                  required
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button 
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium transition-colors"
                >
                  Anulare
                </button>
                <button 
                  type="submit"
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold transition-colors"
                >
                  Salvează Schimbările
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showFeedbackModal && selectedAppointment && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-1">Evaluare Serviciu Medical</h3>
            <p className="text-xs text-gray-500 mb-4">
              Cum evaluezi serviciul <strong className="text-indigo-600">{selectedAppointment.medicalService?.name}</strong>?
            </p>
            
            <form onSubmit={handleFeedbackSubmit} className="space-y-6">
              <div className="flex flex-col items-center justify-center gap-2">
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider">Atinge pentru a oferi stele</label>
                
                <div className="flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((star) => {
                    const isFilled = star <= (hoverRating || feedbackRating);
                    return (
                      <button
                        type="button"
                        key={star}
                        onClick={() => setFeedbackRating(star)}
                        onMouseEnter={() => setHoverRating(star)}
                        onMouseLeave={() => setHoverRating(0)}
                        className="p-1 transition-transform hover:scale-125 focus:outline-none"
                      >
                        <Star
                          size={32}
                          className={`transition-colors ${
                            isFilled ? 'fill-amber-400 text-amber-400' : 'text-gray-300'
                          }`}
                        />
                      </button>
                    );
                  })}
                </div>
                <span className="text-sm font-bold text-gray-700 mt-1">
                  {feedbackRating} din 5 Stele
                </span>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button 
                  type="button" 
                  onClick={() => setShowFeedbackModal(false)} 
                  className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium transition-colors"
                >
                  Anulare
                </button>
                <button 
                  type="submit" 
                  className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-xl text-sm font-bold shadow-md transition-colors"
                >
                  Trimite Recenzia
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}