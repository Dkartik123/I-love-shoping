import api from '@/lib/api';
import type {
  ApiResponse,
  Product,
  Page,
  ProductSearchParams,
} from '@/types';

export const productApi = {
  // Get all products with pagination
  getProducts: async (params?: ProductSearchParams): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products', { params });
    return response.data.data;
  },

  // Get product by ID
  getProductById: async (id: string): Promise<Product> => {
    const response = await api.get<ApiResponse<Product>>(`/products/${id}`);
    return response.data.data;
  },

  // Get product by slug
  getProductBySlug: async (slug: string): Promise<Product> => {
    const response = await api.get<ApiResponse<Product>>(`/products/slug/${slug}`);
    return response.data.data;
  },

  // Search products with filters
  searchProducts: async (params: ProductSearchParams): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products/search', { params });
    return response.data.data;
  },

  // Get search suggestions
  getSuggestions: async (query: string): Promise<string[]> => {
    const response = await api.get<ApiResponse<string[]>>('/products/suggestions', {
      params: { q: query },
    });
    return response.data.data;
  },

  // Get featured products
  getFeaturedProducts: async (page = 0, size = 8): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products/featured', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Get new arrivals
  getNewArrivals: async (page = 0, size = 8): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products/new-arrivals', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Get products on sale
  getOnSaleProducts: async (page = 0, size = 8): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products/on-sale', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Get best sellers
  getBestSellers: async (page = 0, size = 8): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>('/products/best-sellers', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Get products by category
  getByCategory: async (
    categoryId: string,
    page = 0,
    size = 12
  ): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>(
      `/products/category/${categoryId}`,
      { params: { page, size } }
    );
    return response.data.data;
  },

  // Get products by brand
  getByBrand: async (brandId: string, page = 0, size = 12): Promise<Page<Product>> => {
    const response = await api.get<ApiResponse<Page<Product>>>(
      `/products/brand/${brandId}`,
      { params: { page, size } }
    );
    return response.data.data;
  },

  // Get price range
  getPriceRange: async (categoryId?: string): Promise<{ min: number; max: number }> => {
    const response = await api.get<ApiResponse<{ min: number; max: number }>>(
      '/products/price-range',
      { params: categoryId ? { categoryId } : {} }
    );
    return response.data.data;
  },
};
