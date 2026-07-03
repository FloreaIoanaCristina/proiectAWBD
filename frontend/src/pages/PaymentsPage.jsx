import { useState, useEffect, useMemo, useCallback } from 'react';
import { paymentService } from '../services/api';
import { CreditCard, Calendar, AlertCircle, CheckCircle, Search, PlusCircle, Edit2 } from 'lucide-react';

export default function PaymentsPage() {
  const [payments, setPayments] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  const [showTxModal, setShowTxModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState(null);

  const [txAmount, setTxAmount] = useState('');
  const [txAppointmentId, setTxAppointmentId] = useState('');
  const [txStatus, setTxStatus] = useState('PENDING');
  const [txMethod, setTxMethod] = useState('NESPECIFICAT');

  const currentUser = useMemo(() => {
    const savedUser = localStorage.getItem('med_user');
    return savedUser ? JSON.parse(savedUser) : null;
  }, []);

  const userRole = currentUser && currentUser.roles ? currentUser.roles[0] : (currentUser?.role || null);
  const isPatient = userRole === 'ROLE_PATIENT' || userRole === 'PATIENT';
  const isDoctor = userRole === 'ROLE_DOCTOR' || userRole === 'DOCTOR';

  const fetchPayments = useCallback(async () => {
    try {
      setIsLoading(true);
      let res;
      if (isPatient) {
        const patientId = currentUser.profileId || currentUser.id;
        res = await paymentService.getByPatientId(patientId);
      } else {
        res = await paymentService.getAll();
      }
      setPayments(res.data || []);
    } catch (err) {
      setError('Nu s-a putut încărca istoricul plăților.');
    } finally {
      setIsLoading(false);
    }
  }, [currentUser, isPatient]);

  useEffect(() => {
    if (currentUser) {
      fetchPayments();
    }
  }, [currentUser, fetchPayments]);

  const filteredPayments = useMemo(() => {
    return payments.filter(pay => {
      const serviceName = pay.appointment?.medicalService?.name?.toLowerCase() || '';
      const txId = `#trz-${pay.id}`.toLowerCase();
      const patientName = pay.appointment?.patient?.name?.toLowerCase() || '';
      return serviceName.includes(searchTerm.toLowerCase()) || 
             txId.includes(searchTerm.toLowerCase()) ||
             patientName.includes(searchTerm.toLowerCase());
    });
  }, [payments, searchTerm]);

  const openCreateTxModal = () => {
    setIsEditMode(false);
    setSelectedId(null);
    setTxAmount('');
    setTxAppointmentId('');
    setTxStatus('PENDING');
    setTxMethod('NESPECIFICAT');
    setError('');
    setShowTxModal(true);
  };

  const openEditTxModal = (pay) => {
    setIsEditMode(true);
    setSelectedId(pay.id);
    setTxAmount(pay.amount || '');
    setTxAppointmentId(pay.appointment?.id || '');
    setTxStatus(pay.status || 'PENDING');
    setTxMethod(pay.paymentMethod || 'NESPECIFICAT');
    setError('');
    setShowTxModal(true);
  };

const handleTxSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const formattedMethod = txMethod === 'NESPECIFICAT' ? null : txMethod;

    const payload = {
      amount: parseFloat(txAmount),
      appointmentId: txAppointmentId ? parseInt(txAppointmentId) : null,
      status: txStatus,
      paymentMethod: formattedMethod
    };

    try {
      if (isEditMode) {
        await paymentService.update(selectedId, payload);
        setSuccess(`Tranzacția #TRZ-${selectedId} a fost actualizată.`);
      } else {
        await paymentService.create(payload);
        setSuccess('Plata a fost înregistrată cu succes în sistem.');
      }
      setShowTxModal(false);
      fetchPayments();
    } catch (err) {
      setError(err.response?.data?.message || 'Eroare la salvarea tranzacției.');
    }
  };

  const getStatusBadge = (status) => {
    if (status?.toUpperCase() === 'COMPLETED' || status?.toUpperCase() === 'PAID') {
      return (
        <span className="inline-flex items-center gap-1 bg-green-50 text-green-700 px-2.5 py-1 rounded-full text-xs font-semibold border border-green-200">
          <CheckCircle size={12} /> Confirmată
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 bg-amber-50 text-amber-700 px-2.5 py-1 rounded-full text-xs font-semibold border border-amber-200 animate-pulse">
        În așteptare
      </span>
    );
  };

return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Registru Plăți & Încasări</h2>
          <p className="text-sm text-gray-500">
            {isPatient 
              ? 'Urmărește istoricul tranzacțiilor financiare pentru consultațiile tale' 
              : 'Panou centralizat pentru evidența fiscală, încasări și actualizarea statusurilor de plată'}
          </p>
        </div>
        
        <div className="flex items-center gap-2">
          <div className="relative max-w-xs w-full">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
            <input
              type="text"
              placeholder="Caută plată sau serviciu..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white shadow-sm"
            />
          </div>

          {isDoctor && (
            <button onClick={openCreateTxModal} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors shrink-0">
              <PlusCircle size={18} /> Înregistrează Plată
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="p-4 bg-green-50 border-l-4 border-green-500 text-green-700 text-sm rounded-xl">
          {success}
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-md border border-gray-100 overflow-hidden">
        {isLoading ? (
          <div className="p-12 text-center text-gray-400 animate-pulse flex flex-col items-center justify-center gap-2">
            <CreditCard size={32} className="text-indigo-500 animate-bounce" />
            <span>Se încarcă istoricul tranzacțiilor...</span>
          </div>
        ) : filteredPayments.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
                  <th className="py-4 px-6">Referință</th>
                  {!isPatient && <th className="py-4 px-6">Pacient</th>}
                  <th className="py-4 px-6">Data Achitării</th>
                  <th className="py-4 px-6">Metodă</th>
                  <th className="py-4 px-6">Serviciu Medical</th>
                  <th className="py-4 px-6 text-right">Sumă</th>
                  <th className="py-4 px-6 text-center">Status</th>
                  {isDoctor && <th className="py-4 px-6 text-center">Modifică</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                {filteredPayments.map((pay) => (
                  <tr key={pay.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="py-4 px-6 font-mono text-xs font-bold text-indigo-600">
                      #TRZ-{pay.id}
                    </td>
                    {!isPatient && (
                      <td className="py-4 px-6 font-medium text-gray-900">
                        {pay.appointment?.patient?.name || 'Pacient Nespecificat'}
                      </td>
                    )}
                    <td className="py-4 px-6 text-gray-600">
                      <span className="flex items-center gap-1">
                        <Calendar size={14} className="text-gray-400" />
                        {pay.paymentDate ? new Date(pay.paymentDate).toLocaleDateString('ro-RO', {
                          year: 'numeric', month: 'long', day: 'numeric'
                        }) : <span className="text-amber-600 italic text-xs">La confirmare</span>}
                      </span>
                    </td>
                    <td className="py-4 px-6">
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        pay.paymentMethod === 'Card' ? 'bg-blue-50 text-blue-700 border border-blue-100' : 
                        pay.paymentMethod === 'Cash' ? 'bg-teal-50 text-teal-700 border border-teal-100' : 
                        'bg-gray-100 text-gray-500'
                      }`}>
                        {pay.paymentMethod || 'Nespecificat'}
                      </span>
                    </td>
                    <td className="py-4 px-6 font-medium text-gray-800">
                      {pay.appointment?.medicalService?.name || 'Consultație Clinică'}
                    </td>
                    <td className="py-4 px-6 text-right font-bold text-gray-900">
                      {pay.amount?.toFixed(2)} RON
                    </td>
                    <td className="py-4 px-6 text-center">
                      {getStatusBadge(pay.status)}
                    </td>
                    {isDoctor && (
                      <td className="py-4 px-6 text-center">
                        <button onClick={() => openEditTxModal(pay)} className="p-1 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
                          <Edit2 size={14} />
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-16 text-center text-gray-400 flex flex-col items-center justify-center gap-3">
            <CreditCard size={48} className="text-gray-300" />
            <p className="text-base font-medium">Nu s-au găsit înregistrări financiare.</p>
          </div>
        )}
      </div>

      {showTxModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {isEditMode ? `Procesare/Editare Plată #TRZ-${selectedId}` : 'Înregistrare Plată Nouă'}
            </h3>
            
            <form onSubmit={handleTxSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Valoare Totală (RON)</label>
                <input 
                  type="number" step="0.01" min="0" required
                  className="w-full px-3 py-2 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                  value={txAmount} onChange={(e) => setTxAmount(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">ID Programare Asociată</label>
                <input 
                  type="number" required
                  className="w-full px-3 py-2 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                  placeholder="ex: 1042"
                  value={txAppointmentId} onChange={(e) => setTxAppointmentId(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Metodă de Plată</label>
                <select 
                  className="w-full px-3 py-2 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm bg-white"
                  value={txMethod} onChange={(e) => setTxMethod(e.target.value)}
                >
                  <option value="NESPECIFICAT">Nespecificată (În așteptare)</option>
                  <option value="Card">Card</option>
                  <option value="Cash">Cash</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Status Plată</label>
                <select 
                  className="w-full px-3 py-2 border rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm bg-white"
                  value={txStatus} onChange={(e) => setTxStatus(e.target.value)}
                >
                  <option value="PENDING">În așteptare (PENDING)</option>
                  <option value="COMPLETED">Confirmată / Încasată (COMPLETED)</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowTxModal(false)} className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium">Anulare</button>
                <button type="submit" className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold">Salvează</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}