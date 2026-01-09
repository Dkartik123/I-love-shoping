import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, X, Clock, TrendingUp } from 'lucide-react';
import { useSearchStore } from '@/stores/searchStore';
import { productApi } from '@/services/productApi';
import { useDebounce } from '@/hooks/useDebounce';

export default function SearchBar() {
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const {
    suggestions,
    showSuggestions,
    recentSearches,
    setSuggestions,
    setShowSuggestions,
    addRecentSearch,
    clearRecentSearches,
  } = useSearchStore();

  const debouncedQuery = useDebounce(query, 300);

  // Fetch suggestions
  useEffect(() => {
    if (debouncedQuery.length < 2) {
      setSuggestions([]);
      return;
    }

    const fetchSuggestions = async () => {
      setIsLoading(true);
      try {
        const data = await productApi.getSuggestions(debouncedQuery);
        setSuggestions(data);
      } catch (error) {
        setSuggestions([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchSuggestions();
  }, [debouncedQuery, setSuggestions]);

  // Handle click outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [setShowSuggestions]);

  const handleSearch = (searchQuery: string) => {
    if (!searchQuery.trim()) return;
    
    addRecentSearch(searchQuery);
    setShowSuggestions(false);
    navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    setQuery('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(query);
  };

  const handleClear = () => {
    setQuery('');
    inputRef.current?.focus();
  };

  return (
    <div ref={containerRef} className="relative w-full">
      <form onSubmit={handleSubmit} className="relative">
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setShowSuggestions(true)}
          placeholder="Search products..."
          className="w-full pl-10 pr-10 py-2.5 rounded-lg border border-gray-300 focus:border-primary-500 focus:ring-1 focus:ring-primary-500 outline-none transition-colors"
        />
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
        
        {query && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <X className="h-5 w-5" />
          </button>
        )}
      </form>

      {/* Dropdown */}
      {showSuggestions && (query.length > 0 || recentSearches.length > 0) && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-white rounded-lg shadow-lg border overflow-hidden z-50">
          {/* Loading */}
          {isLoading && (
            <div className="px-4 py-3 text-gray-500 text-sm">
              Searching...
            </div>
          )}

          {/* Suggestions */}
          {!isLoading && suggestions.length > 0 && (
            <div>
              <div className="px-4 py-2 bg-gray-50 text-xs font-medium text-gray-500 uppercase">
                Suggestions
              </div>
              {suggestions.map((suggestion, index) => (
                <button
                  key={index}
                  onClick={() => handleSearch(suggestion)}
                  className="w-full px-4 py-2.5 text-left hover:bg-gray-50 flex items-center space-x-3"
                >
                  <TrendingUp className="h-4 w-4 text-gray-400" />
                  <span>{suggestion}</span>
                </button>
              ))}
            </div>
          )}

          {/* Recent searches */}
          {!isLoading && suggestions.length === 0 && recentSearches.length > 0 && (
            <div>
              <div className="px-4 py-2 bg-gray-50 flex items-center justify-between">
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Recent Searches
                </span>
                <button
                  onClick={clearRecentSearches}
                  className="text-xs text-primary-600 hover:underline"
                >
                  Clear all
                </button>
              </div>
              {recentSearches.slice(0, 5).map((search, index) => (
                <button
                  key={index}
                  onClick={() => handleSearch(search)}
                  className="w-full px-4 py-2.5 text-left hover:bg-gray-50 flex items-center space-x-3"
                >
                  <Clock className="h-4 w-4 text-gray-400" />
                  <span>{search}</span>
                </button>
              ))}
            </div>
          )}

          {/* No results */}
          {!isLoading && query.length >= 2 && suggestions.length === 0 && (
            <div className="px-4 py-3 text-gray-500 text-sm">
              No suggestions found for "{query}"
            </div>
          )}
        </div>
      )}
    </div>
  );
}
