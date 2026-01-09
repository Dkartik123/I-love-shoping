import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-9xl font-bold text-gray-200">404</h1>
        <h2 className="text-2xl font-bold text-gray-900 mt-4">Page not found</h2>
        <p className="text-gray-600 mt-2 max-w-md mx-auto">
          Sorry, we couldn't find the page you're looking for. It might have been
          moved or deleted.
        </p>
        <Link to="/" className="btn-primary mt-8 inline-flex items-center gap-2">
          <Home className="h-5 w-5" />
          Back to Home
        </Link>
      </div>
    </div>
  );
}
