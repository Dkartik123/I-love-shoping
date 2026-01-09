import { create } from 'zustand';
import type { ProductSearchParams } from '@/types';

interface SearchState {
  // Search params
  searchParams: ProductSearchParams;
  
  // Search suggestions
  suggestions: string[];
  showSuggestions: boolean;
  
  // Recent searches
  recentSearches: string[];
  
  // Actions
  setSearchQuery: (query: string) => void;
  setSearchParams: (params: Partial<ProductSearchParams>) => void;
  resetFilters: () => void;
  setSuggestions: (suggestions: string[]) => void;
  setShowSuggestions: (show: boolean) => void;
  addRecentSearch: (query: string) => void;
  clearRecentSearches: () => void;
}

const defaultSearchParams: ProductSearchParams = {
  page: 0,
  size: 12,
  sortBy: 'createdAt',
  sortDir: 'desc',
};

export const useSearchStore = create<SearchState>((set, get) => ({
  // Initial state
  searchParams: { ...defaultSearchParams },
  suggestions: [],
  showSuggestions: false,
  recentSearches: JSON.parse(localStorage.getItem('recentSearches') || '[]'),

  // Set search query
  setSearchQuery: (query) =>
    set((state) => ({
      searchParams: { ...state.searchParams, q: query, page: 0 },
    })),

  // Update search params
  setSearchParams: (params) =>
    set((state) => ({
      searchParams: { ...state.searchParams, ...params },
    })),

  // Reset filters to default
  resetFilters: () =>
    set((state) => ({
      searchParams: {
        ...defaultSearchParams,
        q: state.searchParams.q, // Keep the search query
      },
    })),

  // Set suggestions
  setSuggestions: (suggestions) => set({ suggestions }),

  // Show/hide suggestions
  setShowSuggestions: (show) => set({ showSuggestions: show }),

  // Add to recent searches
  addRecentSearch: (query) => {
    const { recentSearches } = get();
    const filtered = recentSearches.filter((s) => s !== query);
    const updated = [query, ...filtered].slice(0, 10);
    localStorage.setItem('recentSearches', JSON.stringify(updated));
    set({ recentSearches: updated });
  },

  // Clear recent searches
  clearRecentSearches: () => {
    localStorage.removeItem('recentSearches');
    set({ recentSearches: [] });
  },
}));
