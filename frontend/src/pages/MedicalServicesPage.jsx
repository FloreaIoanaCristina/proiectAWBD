import { useState, useEffect } from 'react';
import { medicalServiceService, insuranceProviderService } from '../services/api';
import { Stethoscope, Clock, DollarSign, AlertCircle, Sparkles, Activity, X, ShieldCheck, Plus, Pencil, Trash2 } from 'lucide-react';

export default function MedicalServicesPage() {
  const [services, setServices] = useState([]);
  const [insurers, setInsurers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [selectedService, setSelectedService] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const isDoctor = true; 

  const [showUpsertModal, setShowUpsertModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    id: null,
    name: '',
    specialization: '',
    price: '',
    startHour: '08',
    endHour: '16'
  });

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError('');
      const [servicesRes, insurersRes] = await Promise.all([
        medicalServiceService.getAll(),
        insuranceProviderService.getAll().catch(() => ({ data: [] })) 
      ]);

      setServices(servicesRes.data || []);
      const insurersData = insurersRes.data?.content || insurersRes.data || [];
      setInsurers(insurersData);
    } catch (err) {
      setError('Catalogul de servicii sau datele de asigurare nu au putut fi preluate.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const openDetailsModal = (service) => {
    setSelectedService(service);
    setShowModal(true);
  };

  const closeDetailsModal = () => {
    setSelectedService(null);
    setShowModal(false);
  };

  const openCreateModal = () => {
    setIsEditing(false);
    setFormData({ id: null, name: '', specialization: '', price: '', startHour: '08', endHour: '16' });
    setShowUpsertModal(true);
  };

  const openEditModal = (e, service) => {
    e.stopPropagation();
    setIsEditing(true);
    setFormData({
      id: service.id,
      name: service.name,
      specialization: service.specialization || '',
      price: service.price || '',
      startHour: service.startHour || '08',
      endHour: service.endHour || '16'
    });
    setShowUpsertModal(true);
  };

  const handleSaveService = async (e) => {
    e.preventDefault();
    try {
      setError('');
      const payload = {
        ...formData,
        price: parseFloat(formData.price) || 0,
        startHour: formData.startHour,
        endHour: formData.endHour
      };

      if (isEditing) {
        await medicalServiceService.update(formData.id, payload);
        setSuccessMessage('Serviciul medical a fost actualizat cu succes!');
      } else {
        await medicalServiceService.create(payload);
        setSuccessMessage('Serviciul medical a fost adăugat cu succes!');
      }

      setShowUpsertModal(false);
      fetchData();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setError('A apărut o eroare la salvarea serviciului medical.');
      console.error(err);
    }
  };

  const handleDeleteService = async (e, serviceId) => {
    e.stopPropagation();
    if (window.confirm('Ești sigur că vrei să ștergi acest serviciu medical?')) {
      try {
        setError('');
        await medicalServiceService.delete(serviceId);
        setSuccessMessage('Serviciul medical a fost șters.');
        fetchData();
        setTimeout(() => setSuccessMessage(''), 4000);
      } catch (err) {
        setError(err.response?.data?.message || 'Serviciul nu poate fi șters deoarece este asociat unor medici sau programări existente.');
        console.error(err);
      }
    }
  };

  const calculateCoPayment = (basePrice, coveragePercentage) => {
    const price = basePrice || 150.00;
    const coverage = coveragePercentage || 0;
    const finalPrice = price * (1 - coverage / 100);
    return finalPrice.toFixed(2);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Servicii Medicale și Tarife</h2>
          <p className="text-sm text-gray-500">Apasă pe oricare serviciu pentru a vedea prețul simulat prin decontare CAS sau asigurări private</p>
        </div>
        
        {isDoctor && (
          <button
            onClick={openCreateModal}
            className="flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm px-4 py-2.5 rounded-xl shadow-sm transition-colors shrink-0"
          >
            <Plus size={18} />
            Adaugă Serviciu Nou
          </button>
        )}
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {successMessage && (
        <div className="p-4 bg-emerald-50 border-l-4 border-emerald-500 text-emerald-700 text-sm rounded-xl flex items-center gap-2">
          <ShieldCheck size={20} className="text-emerald-600" />
          <span>{successMessage}</span>
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map(n => (
            <div key={n} className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm animate-pulse space-y-3">
              <div className="h-4 bg-gray-200 rounded w-1/4"></div>
              <div className="h-6 bg-gray-200 rounded w-3/4"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2 pt-2"></div>
            </div>
          ))}
        </div>
      ) : services.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {services.map((srv) => (
            <div 
              key={srv.id} 
              onClick={() => openDetailsModal(srv)}
              className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex items-start gap-4 hover:shadow-md hover:border-indigo-200 cursor-pointer transition-all group duration-300 relative"
            >
              <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl group-hover:bg-indigo-600 group-hover:text-white transition-colors shrink-0">
                <Stethoscope size={24} />
              </div>

              <div className="space-y-2 flex-1">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-indigo-600 uppercase tracking-wider bg-indigo-50/50 px-2.5 py-0.5 rounded-md">
                    {srv.specialization || 'General'}
                  </span>
                  
                  <div className="flex items-center gap-2">
                    {isDoctor ? (
                      <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity">
                        <button
                          onClick={(e) => openEditModal(e, srv)}
                          className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Editează Serviciu"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          onClick={(e) => handleDeleteService(e, srv.id)}
                          className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Șterge Serviciu"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    ) : (
                      <span className="text-xs text-gray-400 flex items-center gap-1 font-medium">
                        <Activity size={12} className="text-teal-500" /> ID: #{srv.id}
                      </span>
                    )}
                  </div>
                </div>

                <h3 className="font-bold text-gray-900 text-base group-hover:text-indigo-700 transition-colors">
                  {srv.name}
                </h3>
                
                <p className="text-xs text-gray-400 leading-relaxed line-clamp-2">
                  Disponibil pentru programări în regim ambulatoriu. Apasă pentru detalii și simulare decontare procentuală.
                </p>

                <div className="flex flex-wrap items-center gap-x-4 gap-y-2 pt-3 border-t border-gray-50 text-xs text-gray-500">
                  <span className="flex items-center gap-1 font-bold text-gray-900 bg-emerald-50 text-emerald-700 px-2.5 py-1 rounded-lg border border-emerald-100">
                    <DollarSign size={14} className="text-emerald-600" /> 
                    Tarif: {srv.price ? srv.price.toFixed(2) : '150.00'} RON
                  </span>
                  
                  <span className="flex items-center gap-1 text-gray-500 bg-gray-50 px-2 py-1 rounded-lg border border-gray-100">
                    <Clock size={14} className="text-indigo-400" /> 
                    Orar: {srv.startHour || '08'}:00 - {srv.endHour || '16'}:00
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-16 text-center text-gray-400 bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col items-center justify-center gap-2">
          <Sparkles size={40} className="text-indigo-300" />
          <p className="font-medium text-gray-700">Catalogul este gol în acest moment.</p>
          <p className="text-xs max-w-xs">Contactați departamentul tehnic sau adăugați un serviciu nou de mai sus.</p>
        </div>
      )}

      {showModal && selectedService && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl flex flex-col max-h-[90vh]">
            <div className="flex justify-between items-start mb-4 pb-2 border-b">
              <div>
                <span className="text-xs font-bold text-indigo-600 uppercase tracking-wider bg-indigo-50 px-2.5 py-0.5 rounded">
                  {selectedService.specialization || 'Specialitate Medicală'}
                </span>
                <h3 className="text-lg font-bold text-gray-900 mt-1">{selectedService.name}</h3>
              </div>
              <button onClick={closeDetailsModal} className="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-50">
                <X size={20} />
              </button>
            </div>

            <div className="space-y-4 overflow-y-auto pr-1 flex-1">
              <div className="bg-gray-50 p-4 rounded-xl border border-gray-100 grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="block text-xs font-semibold text-gray-400 uppercase">Tarif Standard Clinica</span>
                  <span className="text-base font-bold text-gray-900">{(selectedService.price || 150.00).toFixed(2)} RON</span>
                </div>
                <div>
                  <span className="block text-xs font-semibold text-gray-400 uppercase">Interval Funcționare</span>
                  <span className="text-sm font-medium text-gray-700">{selectedService.startHour || '08'}:00 - {selectedService.endHour || '16'}:00</span>
                </div>
              </div>

              <div>
                <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 flex items-center gap-1">
                  <ShieldCheck size={14} className="text-indigo-500" /> Co-Plată & Decontare prin Asigurători
                </h4>
                
                <div className="space-y-2">
                  {insurers.length > 0 ? (
                    insurers.map((insurer) => {
                      const finalPrice = calculateCoPayment(selectedService.price, insurer.coveragePercentage);
                      const isFree = parseFloat(finalPrice) === 0;

                      return (
                        <div key={insurer.id} className="flex items-center justify-between p-3 border border-gray-100 rounded-xl bg-white hover:bg-indigo-50/20 transition-colors">
                          <div>
                            <span className="text-sm font-semibold text-gray-800 block">{insurer.name}</span>
                            <span className="text-xs text-gray-400 flex items-center gap-0.5">
                              Acoperire decontată: {insurer.coveragePercentage}%
                            </span>
                          </div>
                          
                          <div className="text-right">
                            <span className={`text-sm font-bold block ${isFree ? 'text-green-600' : 'text-gray-900'}`}>
                              {isFree ? '0.00 RON (Gratuit)' : `${finalPrice} RON`}
                            </span>
                            <span className="text-[10px] text-gray-400 block font-medium">Preț final pacient</span>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <p className="text-xs text-gray-400 italic bg-gray-50 p-3 rounded-xl text-center">
                      Nu s-au găsit companii de asigurare active în sistem pentru simularea tarifelor.
                    </p>
                  )}
                </div>
              </div>
            </div>

            <div className="pt-4 border-t mt-4 flex justify-end">
              <button onClick={closeDetailsModal} className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-semibold rounded-xl transition-colors">
                Închide Fila
              </button>
            </div>
          </div>
        </div>
      )}

      {showUpsertModal && isDoctor && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <form onSubmit={handleSaveService} className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl flex flex-col space-y-4">
            <div className="flex justify-between items-center pb-2 border-b">
              <h3 className="text-lg font-bold text-gray-900">
                {isEditing ? 'Editează Serviciu Medical' : 'Adaugă Serviciu Medical Nou'}
              </h3>
              <button type="button" onClick={() => setShowUpsertModal(false)} className="text-gray-400 hover:text-gray-600 p-1 rounded-lg">
                <X size={20} />
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Serviciu *</label>
                <input 
                  type="text" required
                  value={formData.name}
                  onChange={e => setFormData({...formData, name: e.target.value})}
                  className="w-full text-sm p-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Ex: Consultanță Cardiologie"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Specializare *</label>
                <input 
                  type="text" required
                  value={formData.specialization}
                  onChange={e => setFormData({...formData, specialization: e.target.value})}
                  className="w-full text-sm p-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Ex: Cardiologie, Pediatrie"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Tarif Standard (RON) *</label>
                <input 
                  type="number" step="0.01" required min="0"
                  value={formData.price}
                  onChange={e => setFormData({...formData, price: e.target.value})}
                  className="w-full text-sm p-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Ex: 200.00"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Ora Început</label>
                  <select 
                    value={formData.startHour}
                    onChange={e => setFormData({...formData, startHour: e.target.value})}
                    className="w-full text-sm p-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    {['06','07','08','09','10','11','12'].map(h => <option key={h} value={h}>{h}:00</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Ora Sfârșit</label>
                  <select 
                    value={formData.endHour}
                    onChange={e => setFormData({...formData, endHour: e.target.value})}
                    className="w-full text-sm p-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    {['14','15','16','17','18','19','20','21','22'].map(h => <option key={h} value={h}>{h}:00</option>)}
                  </select>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t flex justify-end gap-2">
              <button 
                type="button" onClick={() => setShowUpsertModal(false)}
                className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-semibold rounded-xl transition-colors"
              >
                Anulează
              </button>
              <button 
                type="submit"
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-xl transition-colors"
              >
                {isEditing ? 'Salvează Modificările' : 'Creează Serviciu'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}