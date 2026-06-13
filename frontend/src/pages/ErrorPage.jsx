import { Link, useLocation } from 'react-router-dom';
import { AlertTriangle, Home, RefreshCw } from 'lucide-react';

export default function ErrorPage({ type }) {
  const location = useLocation();
  
  const is404 = type === 404 || !type;

  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4">
      <div className="bg-red-50 p-6 rounded-full text-red-500 mb-6 animate-bounce">
        <AlertTriangle size={64} />
      </div>
      
      <h1 className="text-6xl font-extrabold text-gray-900 tracking-tight">
        {is404 ? '404' : '500'}
      </h1>
      
      <h2 className="mt-2 text-2xl font-bold text-gray-800">
        {is404 ? 'Pagina nu a fost găsită' : 'Eroare Internă de Server'}
      </h2>
      
      <p className="mt-4 text-gray-500 max-w-md mx-auto text-base">
        {is404 
          ? `Ne pare rău, dar pagina "${location.pathname}" nu există sau a fost mutată în cadrul sistemului MedManager.`
          : 'A apărut o problemă neprevăzută pe serverul medical. Echipa tehnică a fost notificată. Vă rugăm să reîncercați.'
        }
      </p>

      <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
        <Link
          to="/"
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl font-semibold text-sm transition-colors shadow-sm"
        >
          <Home size={18} />
          Pagina Principală
        </Link>
        
        {!is404 && (
          <button
            onClick={() => window.location.reload()}
            className="flex items-center gap-2 bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 px-5 py-2.5 rounded-xl font-semibold text-sm transition-colors shadow-sm"
          >
            <RefreshCw size={18} />
            Reîncarcă Pagina
          </button>
        )}
      </div>
    </div>
  );
}