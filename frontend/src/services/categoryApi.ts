import api from '@/lib/api';
import type { ApiResponse, Category } from '@/types';

export const categoryApi = {
  // Get all categories
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>('/categories');
    return response.data.data;
  },

  // Get category tree
  getCategoryTree: async (): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>('/categories/tree');
    return response.data.data;
  },

  // Get category by ID
  getCategoryById: async (id: string): Promise<Category> => {
    const response = await api.get<ApiResponse<Category>>(`/categories/${id}`);
    return response.data.data;
  },

  // Get category by slug
  getCategoryBySlug: async (slug: string): Promise<Category> => {
    const response = await api.get<ApiResponse<Category>>(`/categories/slug/${slug}`);
    return response.data.data;
  },

  // Get subcategories
  getSubcategories: async (parentId: string): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>(
      `/categories/${parentId}/subcategories`
    );
    return response.data.data;
  },
};
