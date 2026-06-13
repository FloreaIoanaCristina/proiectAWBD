import { useState, useEffect, useCallback } from 'react';
import { insuranceProviderService } from '../services/api';
import { ShieldCheck, ShieldAlert, PlusCircle, Edit2, Trash2, ArrowUpDown, Percent } from 'lucide-react';

export default function InsuranceProvidersPage() {
  const [providers, setProviders] = useState([]);
  
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState(null);

  const [name, setName] = useState('');
  const [coveragePercentage, setCoveragePercentage] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchProviders = useCallback(async () => {
    try {
      const params = {
        page: page,
        size: size,
        sort: `${sortBy},${sortDir}`
      };
      const response = await insuranceProviderService.getPaged(params);
      
      setProviders(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Nu s-a putut încărca lista furnizorilor de asigurări.');
    }
  }, [page, size, sortBy, sortDir]);

  useEffect(() => {
    fetchProviders();
  }, [fetchProviders]);

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
    setCoveragePercentage('');
    setError('');
    setShowModal(true);
  };

  const openEditModal = (provider) => {
    setIsEditMode(true);
    setSelectedId(provider.id);
    setName(provider.name);
    setCoveragePercentage(provider.coveragePercentage ?? '');
    setError('');
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (name.trim().length < 2) {
      setError('Numele asiguratorului trebuie să conțină cel puțin 2 caractere.');
      return;
    }
    
    const percentage = parseFloat(coveragePercentage);
    if (isNaN(percentage) || percentage < 0 || percentage > 100) {
      setError('Procentul de decontare/acoperire trebuie să fie o valoare numerică între 0 și 100.');
      return;
    }

    const payload = {
      name: name.trim(),
      coveragePercentage: percentage
    };

    try {
      if (isEditMode) {
        await insuranceProviderService.update(selectedId, payload);
        setSuccess(`Furnizorul ${payload.name} a fost actualizat.`);
      } else {
        await insuranceProviderService.create(payload);
        setSuccess(`Furnizorul ${payload.name} a fost adăugat cu succes.`);
      }
      setShowModal(false);
      fetchProviders();
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la transmiterea datelor către server.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Sigur doriți să eliminați acest furnizor de asigurări? Această operațiune poate eșua dacă există pacienți arondați.')) return;
    setError('');
    setSuccess('');

    try {
      await insuranceProviderService.delete(id);
      setSuccess('Furnizorul a fost șters din baza de date.');
      fetchProviders();
    } catch (err) {
      setError(err.response?.data?.message || 'Nu se poate șterge asiguratorul deoarece există pacienți asigurați prin această companie.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Companii de Asigurări</h2>
          <p className="text-sm text-gray-500">Gestiune parteneri, contracte de asigurare de sănătate și procente de decontare</p>
        </div>
        
        <button 
          onClick={openCreateModal}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors"
        >
          <PlusCircle size={18} />
          Adaugă Asigurator
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
              <th className="py-4 px-6">ID Contract</th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('name')}>
                <div className="flex items-center gap-1">Nume Companie <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('coveragePercentage')}>
                <div className="flex items-center gap-1">Acoperire / Decontare <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6 text-center">Acțiuni</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
            {providers.length > 0 ? (
              providers.map((provider) => (
                <tr key={provider.id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="py-4 px-6 font-semibold text-gray-400">#{provider.id}</td>
                  <td className="py-4 px-6 font-medium text-gray-900">
                    <div className="flex items-center gap-2">
                      <ShieldCheck size={16} className="text-indigo-500" />
                      {provider.name}
                    </div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex items-center gap-1 font-semibold text-gray-700">
                      {provider.coveragePercentage}%
                      <Percent size={14} className="text-gray-400" />
                    </div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex justify-center items-center gap-3">
                      <button 
                        onClick={() => openEditModal(provider)}
                        className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Modifică contract"
                      >
                        <Edit2 size={16} />
                      </button>
                      <button 
                        onClick={() => handleDelete(provider.id)}
                        className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Elimină furnizor"
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
                  Nu există furnizori de asigurări înregistrați în sistem.
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
              {isEditMode ? 'Actualizare Contract Asigurări' : 'Nomenclator Asigurator Nou'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Companie Asigurări</label>
                <input 
                  type="text"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: CASMB, Allianz, Omniasig"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Procent Decontat (%)</label>
                <input 
                  type="number"
                  min="0"
                  max="100"
                  step="0.1"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: 100 pentru decontare integrală, 75 pentru parțială"
                  value={coveragePercentage}
                  onChange={(e) => setCoveragePercentage(e.target.value)}
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
                  {isEditMode ? 'Salvează Schimbările' : 'Adaugă Furnizor'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}