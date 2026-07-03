import { useState, useEffect, useCallback } from 'react';
import { insuranceProviderService, medicalServiceService, serviceCoverageService } from '../services/api';
import { 
  ShieldCheck, ShieldAlert, PlusCircle, Edit2, Trash2, 
  ArrowUpDown, Phone, Eye, X, Plus
} from 'lucide-react';

export default function InsuranceProvidersPage() {
  const [providers, setProviders] = useState([]);
  const [medicalServices, setMedicalServices] = useState([]);
  
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  const isDoctor = true;

  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [name, setName] = useState('');
  const [contactNumber, setContactNumber] = useState('');

  const [showCoverageModal, setShowCoverageModal] = useState(false);
  const [currentInsurer, setCurrentInsurer] = useState(null);
  const [coverages, setCoverages] = useState([]);
  

  const [isEditingCoverage, setIsEditingCoverage] = useState(false);
  const [selectedCoverageId, setSelectedCoverageId] = useState(null);
  const [selectedServiceId, setSelectedServiceId] = useState('');
  const [specificCoveragePercentage, setSpecificCoveragePercentage] = useState('');

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

  const fetchMedicalServices = useCallback(async () => {
    try {
      const response = await medicalServiceService.getAll();
      setMedicalServices(response.data || []);
    } catch (err) {
      console.error('Nu s-au putut încărca serviciile medicale pentru mapare.');
    }
  }, []);

  useEffect(() => {
    fetchProviders();
    fetchMedicalServices();
  }, [fetchProviders, fetchMedicalServices]);

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
    setContactNumber('');
    setError('');
    setShowModal(true);
  };

  const openEditModal = (item) => {
    setIsEditMode(true);
    setSelectedId(item.id);
    setName(item.name);
    setContactNumber(item.contactNumber || '');
    setError('');
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (name.trim().length < 2) {
      setError('Numele trebuie să conțină cel puțin 2 caractere.');
      return;
    }

    try {
      const payload = { name: name.trim(), contactNumber: contactNumber.trim() || null };
      if (isEditMode) {
        await insuranceProviderService.update(selectedId, payload);
        setSuccess(`Asiguratorul ${payload.name} a fost actualizat.`);
      } else {
        await insuranceProviderService.create(payload);
        setSuccess(`Asiguratorul ${payload.name} a fost adăugat.`);
      }
      fetchProviders();
      setShowModal(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la salvarea datelor.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Sigur doriți să ștergeți această înregistrare? Acțiunea poate eșua dacă resursa este deja utilizată.')) return;
    setError('');
    setSuccess('');

    try {
      await insuranceProviderService.delete(id);
      fetchProviders();
      setSuccess('Înregistrarea a fost eliminată cu succes.');
    } catch (err) {
      setError(err.response?.data?.message || 'Ștergerea a eșuat. Resursa are constrângeri active.');
    }
  };

  const fetchAllCoveragesFiltered = async (insurerId) => {
    try {
      const response = await serviceCoverageService.getAll();
      const allCoverages = response.data || [];
      const filtered = allCoverages.filter(cov => 
        cov.insuranceProviderId === insurerId || cov.insuranceProvider?.id === insurerId
      );
      setCoverages(filtered);
    } catch (err) {
      console.error('Eroare la preluarea acoperirilor.');
    }
  };

  const openCoverageManagement = (insurer) => {
    setCurrentInsurer(insurer);
    fetchAllCoveragesFiltered(insurer.id);
    resetCoverageForm();
    setShowCoverageModal(true);
  };

  const resetCoverageForm = () => {
    setIsEditingCoverage(false);
    setSelectedCoverageId(null);
    setSelectedServiceId('');
    setSpecificCoveragePercentage('');
  };

  const handleSaveCoverage = async (e) => {
    e.preventDefault();
    if (!selectedServiceId || !specificCoveragePercentage) return;

    const percentage = parseFloat(specificCoveragePercentage);
    if (isNaN(percentage) || percentage < 0 || percentage > 100) {
      alert('Procentul trebuie să fie între 0 și 100.');
      return;
    }

    try {
      const coverageDto = {
        insuranceProviderId: currentInsurer.id,
        medicalServiceId: parseInt(selectedServiceId),
        coveragePercent: percentage
      };

      if (isEditingCoverage) {
        await serviceCoverageService.update(selectedCoverageId, coverageDto);
      } else {
        await serviceCoverageService.create(coverageDto);
      }

      fetchAllCoveragesFiltered(currentInsurer.id);
      resetCoverageForm();
    } catch (err) {
      alert(err.response?.data?.message || 'Eroare la salvarea acoperirii specifice.');
    }
  };

  const handleEditCoverageClick = (cov) => {
    setIsEditingCoverage(true);
    setSelectedCoverageId(cov.id);
    setSelectedServiceId(cov.medicalService?.id || cov.medicalServiceId || '');
    setSpecificCoveragePercentage(cov.coveragePercentage);
  };

  const handleDeleteCoverage = async (coverageId) => {
    if (!window.confirm('Sigur doriți să ștergeți această acoperire specifică?')) return;
    try {
      await serviceCoverageService.delete(coverageId);
      fetchAllCoveragesFiltered(currentInsurer.id);
    } catch (err) {
      alert('Eroare la ștergerea acoperirii.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Companii Asigurări & Acoperiri</h2>
          <p className="text-sm text-gray-500">Gestiune parteneri asiguratori și configurarea decontărilor specifice pe servicii medicale</p>
        </div>
        
        {isDoctor && (
          <button 
            onClick={openCreateModal}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors shrink-0"
          >
            <PlusCircle size={18} />
            Adaugă Asigurator Nou
          </button>
        )}
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
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
                <th className="py-4 px-6">ID Contract</th>
                <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('name')}>
                  <div className="flex items-center gap-1">Nume Companie <ArrowUpDown size={14} /></div>
                </th>
                <th className="py-4 px-6">Număr de Contact</th>
                <th className="py-4 px-6 text-center">Acoperiri Servicii</th>
                <th className="py-4 px-6 text-center">Acțiuni General</th>
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

                    <td className="py-4 px-6 text-gray-600">
                      {provider.contactNumber ? (
                        <div className="flex items-center gap-1.5">
                          <Phone size={14} className="text-gray-400" />
                          <span>{provider.contactNumber}</span>
                        </div>
                      ) : (
                        <span className="text-gray-400 italic text-xs">Nespecificat</span>
                      )}
                    </td>
                    
                    <td className="py-4 px-6 text-center">
                      <button
                        onClick={() => openCoverageManagement(provider)}
                        className="inline-flex items-center gap-1 text-xs font-bold bg-indigo-50 text-indigo-700 hover:bg-indigo-100 px-3 py-1.5 rounded-xl transition-colors"
                      >
                        <Eye size={14} />
                        {isDoctor ? 'Configurează / Vezi Servicii' : 'Vezi Servicii Decontate'}
                      </button>
                    </td>

                    <td className="py-4 px-6">
                      <div className="flex justify-center items-center gap-2">
                        {isDoctor ? (
                          <>
                            <button onClick={() => openEditModal(provider)} className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Modifică date partener">
                              <Edit2 size={16} />
                            </button>
                            <button onClick={() => handleDelete(provider.id)} className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Elimină partener">
                              <Trash2 size={16} />
                            </button>
                          </>
                        ) : (
                          <span className="text-xs text-gray-400 italic">Doar vizualizare</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="py-8 text-center text-gray-400">Nu există furnizori de asigurări înregistrați.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="bg-gray-50 px-6 py-4 border-t border-gray-100 flex items-center justify-between text-sm">
            <span className="text-gray-500">
              Pagina <strong className="text-gray-700">{page + 1}</strong> din <strong className="text-gray-700">{totalPages}</strong>
            </span>
            <div className="flex gap-2">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 font-medium transition-colors">Anterior</button>
              <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)} className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 font-medium transition-colors">Următor</button>
            </div>
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {isEditMode ? 'Actualizare Contract Asigurări' : 'Nomenclator Asigurator Nou'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Companie</label>
                <input 
                  type="text" required
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: CASMB, Allianz"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  disabled={!isDoctor}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Număr Telefon Contact</label>
                <input 
                  type="text"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="ex: +407xxxxxxxx"
                  value={contactNumber}
                  onChange={(e) => setContactNumber(e.target.value)}
                  disabled={!isDoctor}
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium">
                  Anulare
                </button>
                {isDoctor && (
                  <button type="submit" className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold">
                    {isEditMode ? 'Salvează Schimbările' : 'Adaugă Furnizor'}
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {showCoverageModal && currentInsurer && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-xl w-full p-6 shadow-2xl flex flex-col max-h-[85vh]">
            
            <div className="flex justify-between items-center pb-3 border-b mb-4">
              <div>
                <h3 className="text-lg font-bold text-gray-900">Decontări Procentuale: {currentInsurer.name}</h3>
                <p className="text-xs text-gray-500">Configurați procente specifice de acoperire pentru serviciile medicale de mai jos</p>
              </div>
              <button onClick={() => setShowCoverageModal(false)} className="text-gray-400 hover:text-gray-600 p-1 rounded-lg">
                <X size={20} />
              </button>
            </div>

            {isDoctor && (
              <form onSubmit={handleSaveCoverage} className="bg-gray-50 border border-gray-100 p-4 rounded-xl mb-4 space-y-3">
                <h4 className="text-xs font-bold text-indigo-700 tracking-wider">
                  {isEditingCoverage ? 'Modifică Procent de Decontare Serviciu' : 'Adaugă Procent Nou de Decontare'}
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
                  <div className="sm:col-span-2">
                    <label className="block text-[10px] font-bold text-gray-500 uppercase mb-1">Selectează Serviciu Medical</label>
                    <select
                      required
                      value={selectedServiceId}
                      onChange={(e) => setSelectedServiceId(e.target.value)}
                      className="w-full px-3 py-1.5 border bg-white rounded-lg focus:ring-2 focus:ring-indigo-500 text-xs focus:outline-none"
                    >
                      <option value="">Alege un serviciu...</option>
                      {medicalServices.map(srv => (
                        <option key={srv.id} value={srv.id}>{srv.name} ({srv.specialization || 'General'})</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-500 uppercase mb-1">Acoperire (%)</label>
                    <input
                      type="number" min="0" max="100" step="1" required
                      placeholder="Ex: 80"
                      value={specificCoveragePercentage}
                      onChange={(e) => setSpecificCoveragePercentage(e.target.value)}
                      className="w-full px-3 py-1.5 border bg-white rounded-lg focus:ring-2 focus:ring-indigo-500 text-xs focus:outline-none"
                    />
                  </div>
                </div>
                <div className="flex justify-end gap-2 pt-1 text-xs">
                  {isEditingCoverage && (
                    <button type="button" onClick={resetCoverageForm} className="px-3 py-1.5 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium text-gray-700">
                      Anulează
                    </button>
                  )}
                  <button type="submit" className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold flex items-center gap-1">
                    {isEditingCoverage ? 'Salvează modificarea' : <><Plus size={14} /> Adaugă Acoperire</>}
                  </button>
                </div>
              </form>
            )}

            <div className="flex-1 overflow-y-auto pr-1 space-y-2">
              <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Procente de Decontare Active</h4>
              {coverages.length > 0 ? (
                coverages.map((cov) => (
                  <div key={cov.id} className="flex items-center justify-between p-3 border border-gray-100 rounded-xl bg-white hover:bg-gray-50/50 transition-colors text-sm">
                    <div>
                      <span className="font-semibold text-gray-800 block">{cov.medicalService?.name || `Serviciu #${cov.medicalServiceId}`}</span>
                      <span className="text-xs text-gray-400">{cov.medicalService?.specialization || 'Specialitate generală'}</span>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-lg border border-indigo-100 text-xs">
                        Decontat: {cov.coveragePercent}%
                      </span>
                      {isDoctor && (
                        <div className="flex items-center gap-1">
                          <button onClick={() => handleEditCoverageClick(cov)} className="p-1 text-blue-600 hover:bg-blue-50 rounded-md transition-colors">
                            <Edit2 size={14} />
                          </button>
                          <button onClick={() => handleDeleteCoverage(cov.id)} className="p-1 text-red-600 hover:bg-red-50 rounded-md transition-colors">
                            <Trash2 size={14} />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-gray-400 italic text-center py-6 bg-gray-50 rounded-xl border border-dashed">
                  Nu sunt configurate decontări specifice pentru acest asigurator.
                </p>
              )}
            </div>

            <div className="pt-3 border-t mt-4 flex justify-end">
              <button onClick={() => setShowCoverageModal(false)} className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-semibold rounded-xl">
                Închide
              </button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
}