import { useState, useEffect, useCallback } from 'react';
import { doctorService } from '../services/api';
import { UserCog, ShieldAlert, PlusCircle, Edit2, Trash2, ArrowUpDown, Briefcase } from 'lucide-react';

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState([]);
  
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState(null);

  const [name, setName] = useState('');
  const [office, setOffice] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchDoctors = useCallback(async () => {
    try {
      const params = {
        page: page,
        size: size,
        sort: `${sortBy},${sortDir}`
      };

      const response = await doctorService.getPaged(params);
      
      setDoctors(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Nu s-a putut prelua lista de medici de pe server.');
    }
  }, [page, size, sortBy, sortDir]);

  useEffect(() => {
    fetchDoctors();
  }, [fetchDoctors]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const openCreateModal = () => {
    setIsEditMode(false);
    setSelectedId(null);
    setName('');
    setOffice('');
    setError('');
    setShowModal(true);
  };

  const openEditModal = (doctor) => {
    setIsEditMode(true);
    setSelectedId(doctor.id);
    setName(doctor.name);
    setOffice(doctor.office || '');
    setError('');
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (name.trim().length < 3) {
      setError('Numele complet al medicului trebuie să aibă minimum 3 caractere.');
      return;
    }
    if (!office.trim()) {
      setError('Specificarea cabinetului/biroului este obligatorie.');
      return;
    }

    const payload = {
      name: name.trim(),
      office: office.trim()
    };

    try {
      if (isEditMode) {
        await doctorService.update(selectedId, payload);
        setSuccess(`Datele medicului ${payload.name} au fost actualizate.`);
      } else {
        await doctorService.create(payload);
        setSuccess(`Medicul ${payload.name} a fost adăugat cu succes.`);
      }
      setShowModal(false);
      fetchDoctors();
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la procesarea cererii pe server.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Sigur doriți să ștergeți acest medic din sistem? Această acțiune este ireversibilă.')) return;
    setError('');
    setSuccess('');

    try {
      await doctorService.delete(id);
      setSuccess('Medicul a fost eliminat din registrul clinicii.');
      fetchDoctors();
    } catch (err) {
      setError(err.response?.data?.message || 'Ștergerea a eșuat. Medicul are legături active (programări/concedii) în baza de date.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Registru Medici</h2>
          <p className="text-sm text-gray-500">Gestiune personal medical, cabinete alocate și disponibilități</p>
        </div>
        
        <button 
          onClick={openCreateModal}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors"
        >
          <PlusCircle size={18} />
          Adaugă Medic
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2">
          <ShieldAlert size={20} className="shrink-0" />
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
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('name')}>
                <div className="flex items-center gap-1">Nume Medic <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('office')}>
                <div className="flex items-center gap-1">Cabinet Locație <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6 text-center">Acțiuni</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
            {doctors.length > 0 ? (
              doctors.map((doctor) => (
                <tr key={doctor.id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="py-4 px-6 font-semibold text-gray-400">#{doctor.id}</td>
                  <td className="py-4 px-6 font-medium text-gray-900">
                    <div className="flex items-center gap-2">
                      <UserCog size={16} className="text-indigo-500" />
                      {doctor.name}
                    </div>
                  </td>
                  <td className="py-4 px-6 text-gray-500">
                    <div className="flex items-center gap-1">
                      <Briefcase size={14} className="text-gray-400" />
                      {doctor.office || 'Nalocat'}
                    </div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex justify-center items-center gap-3">
                      <button 
                        onClick={() => openEditModal(doctor)}
                        className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Editează detalii medic"
                      >
                        <Edit2 size={16} />
                      </button>
                      <button 
                        onClick={() => handleDelete(doctor.id)}
                        className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Șterge medic"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4" className="py-8 text-center text-gray-400">
                  Nu s-au găsit medici înregistrați în sistem.
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

      {showModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {isEditMode ? 'Actualizare Profil Medic' : 'Înregistrare Medic Nou'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Complet Medic</label>
                <input 
                  type="text"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: Dr. Popescu Andrei"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Cabinet Alocat</label>
                <input 
                  type="text"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: Cabinet 302, Etaj 3"
                  value={office}
                  onChange={(e) => setOffice(e.target.value)}
                  required
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button 
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium transition-colors"
                >
                  Anulare
                </button>
                <button 
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold transition-colors"
                >
                  {isEditMode ? 'Salvează Datele' : 'Înregistrează Medicul'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}