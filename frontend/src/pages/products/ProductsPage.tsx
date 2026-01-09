import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ChevronDown, SlidersHorizontal, X } from 'lucide-react';
import { productApi } from '@/services/productApi';
import { categoryApi } from '@/services/categoryApi';
import ProductGrid from '@/components/products/ProductGrid';
import type { ProductSearchParams } from '@/types';

const SORT_OPTIONS = [
  { value: 'createdAt:desc', label: 'Newest First' },
  { value: 'createdAt:asc', label: 'Oldest First' },
  { value: 'price:asc', label: 'Price: Low to High' },
  { value: 'price:desc', label: 'Price: High to Low' },
  { value: 'name:asc', label: 'Name: A to Z' },
  { value: 'name:desc', label: 'Name: Z to A' },
  { value: 'rating:desc', label: 'Highest Rated' },
  { value: 'soldCount:desc', label: 'Best Selling' },
];

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [showFilters, setShowFilters] = useState(false);

  // Parse search params
  const params: ProductSearchParams = {
    page: parseInt(searchParams.get('page') || '0'),
    size: 12,
    q: searchParams.get('q') || undefined,
    categoryId: searchParams.get('category') || undefined,
    brandId: searchParams.get('brand') || undefined,
    minPrice: searchParams.get('minPrice')
      ? parseFloat(searchParams.get('minPrice')!)
      : undefined,
    maxPrice: searchParams.get('maxPrice')
      ? parseFloat(searchParams.get('maxPrice')!)
      : undefined,
    inStock: searchParams.get('inStock') === 'true' ? true : undefined,
    onSale: searchParams.get('onSale') === 'true' ? true : undefined,
    sortBy: (searchParams.get('sortBy') as ProductSearchParams['sortBy']) || 'createdAt',
    sortDir: (searchParams.get('sortDir') as ProductSearchParams['sortDir']) || 'desc',
  };

  // Fetch products
  const { data: productsData, isLoading } = useQuery({
    queryKey: ['products', params],
    queryFn: () =>
      params.q ? productApi.searchProducts(params) : productApi.getProducts(params),
  });

  // Fetch categories for filter
  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getCategories(),
  });

  // Fetch price range
  const { data: priceRange } = useQuery({
    queryKey: ['priceRange', params.categoryId],
    queryFn: () => productApi.getPriceRange(params.categoryId),
  });

  const updateParams = (updates: Record<string, string | null>) => {
    const newParams = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([key, value]) => {
      if (value === null || value === '') {
        newParams.delete(key);
      } else {
        newParams.set(key, value);
      }
    });
    newParams.set('page', '0'); // Reset to first page
    setSearchParams(newParams);
  };

  const handleSortChange = (value: string) => {
    const [sortBy, sortDir] = value.split(':');
    updateParams({ sortBy, sortDir });
  };

  const clearFilters = () => {
    setSearchParams({});
  };

  const hasActiveFilters =
    params.categoryId ||
    params.brandId ||
    params.minPrice ||
    params.maxPrice ||
    params.inStock ||
    params.onSale;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {params.q ? `Search results for "${params.q}"` : 'All Products'}
          </h1>
          {productsData && (
            <p className="text-gray-500 mt-1">
              {productsData.totalElements} products found
            </p>
          )}
        </div>

        <div className="flex items-center gap-4">
          {/* Mobile filter toggle */}
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="lg:hidden btn-secondary flex items-center gap-2"
          >
            <SlidersHorizontal className="h-4 w-4" />
            Filters
          </button>

          {/* Sort dropdown */}
          <div className="relative">
            <select
              value={`${params.sortBy}:${params.sortDir}`}
              onChange={(e) => handleSortChange(e.target.value)}
              className="input pr-10 appearance-none cursor-pointer"
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
          </div>
        </div>
      </div>

      <div className="flex gap-8">
        {/* Filters Sidebar */}
        <aside
          className={`${
            showFilters ? 'fixed inset-0 z-50 bg-white p-6' : 'hidden'
          } lg:block lg:relative lg:inset-auto lg:z-auto lg:bg-transparent lg:p-0 lg:w-64 flex-shrink-0`}
        >
          <div className="flex items-center justify-between lg:hidden mb-6">
            <h2 className="text-lg font-semibold">Filters</h2>
            <button onClick={() => setShowFilters(false)}>
              <X className="h-6 w-6" />
            </button>
          </div>

          {hasActiveFilters && (
            <button
              onClick={clearFilters}
              className="text-sm text-primary-600 hover:underline mb-4"
            >
              Clear all filters
            </button>
          )}

          {/* Categories */}
          <div className="mb-6">
            <h3 className="font-semibold mb-3">Category</h3>
            <div className="space-y-2">
              {categories?.map((category) => (
                <label key={category.id} className="flex items-center">
                  <input
                    type="radio"
                    name="category"
                    checked={params.categoryId === category.id}
                    onChange={() =>
                      updateParams({
                        category: params.categoryId === category.id ? null : category.id,
                      })
                    }
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    {category.name} ({category.productCount})
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Price Range */}
          <div className="mb-6">
            <h3 className="font-semibold mb-3">Price Range</h3>
            <div className="flex items-center gap-2">
              <input
                type="number"
                placeholder={priceRange ? `$${priceRange.min}` : 'Min'}
                value={params.minPrice || ''}
                onChange={(e) => updateParams({ minPrice: e.target.value || null })}
                className="input w-24"
              />
              <span className="text-gray-400">-</span>
              <input
                type="number"
                placeholder={priceRange ? `$${priceRange.max}` : 'Max'}
                value={params.maxPrice || ''}
                onChange={(e) => updateParams({ maxPrice: e.target.value || null })}
                className="input w-24"
              />
            </div>
          </div>

          {/* Availability */}
          <div className="mb-6">
            <h3 className="font-semibold mb-3">Availability</h3>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={params.inStock === true}
                onChange={(e) =>
                  updateParams({ inStock: e.target.checked ? 'true' : null })
                }
                className="h-4 w-4 text-primary-600 rounded focus:ring-primary-500"
              />
              <span className="ml-2 text-sm text-gray-700">In Stock Only</span>
            </label>
          </div>

          {/* On Sale */}
          <div className="mb-6">
            <h3 className="font-semibold mb-3">Offers</h3>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={params.onSale === true}
                onChange={(e) =>
                  updateParams({ onSale: e.target.checked ? 'true' : null })
                }
                className="h-4 w-4 text-primary-600 rounded focus:ring-primary-500"
              />
              <span className="ml-2 text-sm text-gray-700">On Sale</span>
            </label>
          </div>

          {/* Mobile apply button */}
          <button
            onClick={() => setShowFilters(false)}
            className="lg:hidden btn-primary w-full mt-6"
          >
            Apply Filters
          </button>
        </aside>

        {/* Products Grid */}
        <div className="flex-1">
          <ProductGrid
            products={productsData?.content || []}
            isLoading={isLoading}
            emptyMessage={
              params.q
                ? `No products found for "${params.q}"`
                : 'No products found'
            }
          />

          {/* Pagination */}
          {productsData && productsData.totalPages > 1 && (
            <div className="flex justify-center mt-8 gap-2">
              <button
                onClick={() =>
                  updateParams({ page: String(productsData.number - 1) })
                }
                disabled={productsData.first}
                className="btn-secondary disabled:opacity-50"
              >
                Previous
              </button>
              <span className="flex items-center px-4 text-gray-600">
                Page {productsData.number + 1} of {productsData.totalPages}
              </span>
              <button
                onClick={() =>
                  updateParams({ page: String(productsData.number + 1) })
                }
                disabled={productsData.last}
                className="btn-secondary disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
