import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { productApi } from '@/services/productApi';
import ProductGrid from '@/components/products/ProductGrid';

export default function SearchPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const page = parseInt(searchParams.get('page') || '0');

  const { data: products, isLoading } = useQuery({
    queryKey: ['search', query, page],
    queryFn: () => productApi.searchProducts({ q: query, page, size: 12 }),
    enabled: !!query,
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Search results for "{query}"
        </h1>
        {products && (
          <p className="mt-1 text-gray-500">
            {products.totalElements} products found
          </p>
        )}
      </div>

      <ProductGrid
        products={products?.content || []}
        isLoading={isLoading}
        emptyMessage={`No products found for "${query}"`}
      />
    </div>
  );
}
