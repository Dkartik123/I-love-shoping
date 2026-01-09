import { useParams, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { categoryApi } from '@/services/categoryApi';
import { productApi } from '@/services/productApi';
import ProductGrid from '@/components/products/ProductGrid';

export default function CategoryPage() {
  const { slug } = useParams<{ slug: string }>();
  const [searchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') || '0');

  const { data: category } = useQuery({
    queryKey: ['category', slug],
    queryFn: () => categoryApi.getCategoryBySlug(slug!),
    enabled: !!slug,
  });

  const { data: products, isLoading } = useQuery({
    queryKey: ['products', 'category', category?.id, page],
    queryFn: () => productApi.getByCategory(category!.id, page),
    enabled: !!category?.id,
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {category?.name || 'Category'}
        </h1>
        {category?.description && (
          <p className="mt-2 text-gray-600">{category.description}</p>
        )}
      </div>

      <ProductGrid
        products={products?.content || []}
        isLoading={isLoading}
        emptyMessage={`No products found in ${category?.name || 'this category'}`}
      />
    </div>
  );
}
