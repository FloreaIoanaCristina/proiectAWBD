import { useState, useEffect, useCallback, useMemo } from 'react';
import { appointmentService, medicalServiceService, patientService } from '../services/api';
import { Calendar, Clock, User, UserCog, Trash2, Edit2, PlusCircle, AlertCircle, ArrowUpDown } from 'lucide-react';

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
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  
  const [patientId, setPatientId] = useState('');
  const [medicalServiceId, setMedicalServiceId] = useState('');
  const [createDate, setCreateDate] = useState('');

  const [editDate, setEditDate] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [patientNamePreview, setPatientNamePreview] = useState('');
  const [isSearchingPatient, setIsSearchingPatient] = useState(false);

  const currentUser = useMemo(() => JSON.parse(localStorage.getItem('med_user')), []);
  console.log(currentUser);

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
        response = await appointmentService.getByPatientId(profileId);
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
}, [medicalServiceId, medicalServices]);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const isUserPatient = currentUser.role === 'PATIENT';

    if (!isUserPatient && !patientId) {
      setError('ID-ul pacientului este obligatoriu pentru personalul medical.');
      return;
    }

    if (!medicalServiceId || !createDate) {
      setError('Toate câmpurile sunt obligatorii.');
      return;
    }

    if (new Date(createDate) < new Date()) {
      setError('Nu puteți face o programare în trecut.');
      return;
    }

    try {
      const payload = {
        patientId: isUserPatient ?  null : parseInt(patientId),
        medicalServiceId: parseInt(medicalServiceId),
        doctorId: doctorId ? parseInt(doctorId) : null,
        appointmentFrom: new Date(createDate).toISOString()
      };

      const response = await appointmentService.create(payload);
      setSuccess('Programarea a fost creată cu succes!');
      setShowCreateModal(false);
      setPatientId('');
      setMedicalServiceId('');
      setCreateDate('');
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la crearea programării. Verificați sloturile disponibile.');
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
                        {new Date(app.appointmentFrom).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex justify-center items-center gap-3">
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
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="py-8 text-center text-gray-400 bg-gray-50/20">
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
              {currentUser.role !== 'PATIENT' && (
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
                    <option value="">Nu există medici disponibili pentru acest serviciu</option>
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
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Data și Ora</label>
                <input 
                  type="datetime-local"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  value={createDate}
                  onChange={(e) => setCreateDate(e.target.value)}
                  required
                />
              </div>

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
    </div>
  );
}