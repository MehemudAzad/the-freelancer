import api from '@/lib/api';
import { User } from '@/types/api';

class FreelancerService {
  // Search for freelancers by handle
  async searchFreelancers(query: string): Promise<User[]> {
    try {
      console.log('🔍 FreelancerService: Searching freelancers with query:', query);
      
      // Use the working API endpoint with handle parameter
      try {
        console.log('🔍 Searching with handle parameter:', query);
        const response = await api.get('/api/auth/public/users/search', {
          params: { 
            handle: query,
            page: 0,
            size: 10
          }
        });
        
        const users = response.data.content || response.data || [];
        console.log('🔍 Raw users from API:', users);
        
        // Filter only freelancers (handle both uppercase and lowercase)
        const freelancers = users.filter((user: User) => 
          user.role === 'freelancer' || (user.role as string) === 'FREELANCER'
        );
        
        console.log('🔍 Filtered freelancers:', freelancers);
        
        return freelancers;
      } catch (error) {
        console.log('🔍 Search failed:', error);
        return [];
      }
      
    } catch (error) {
      console.error('❌ FreelancerService: Error searching freelancers:', error);
      throw error;
    }
  }

  // Get all freelancers using the search API with different handles
  async getAllFreelancers(): Promise<User[]> {
    try {
      console.log('🔍 FreelancerService: Getting all freelancers using search API');
      
      // Use the search API that works - try with common handles to get users
      const commonHandles = ['a', 'e', 'i', 'o', 'u', 'admin', 'user', 'test'];
      const allUsers: User[] = [];
      const seenIds = new Set<number>();
      
      for (const handle of commonHandles) {
        try {
          console.log(`🔍 Searching with handle: "${handle}"`);
          const response = await api.get('/api/auth/public/users/search', {
            params: { 
              handle: handle,
              page: 0,
              size: 10
            }
          });
          
          const users = response.data.content || response.data || [];
          console.log(`🔍 Found ${users.length} users for handle "${handle}":`, users);
          
          // Add unique users to our collection
          users.forEach((user: User) => {
            if (!seenIds.has(user.id)) {
              seenIds.add(user.id);
              allUsers.push(user);
            }
          });
          
          // Small delay to avoid rate limiting
          await new Promise(resolve => setTimeout(resolve, 100));
        } catch (error) {
          console.log(`🔍 Search failed for handle "${handle}":`, error);
          continue;
        }
      }
      
      console.log('🔍 All unique users found:', allUsers);
      
      // Filter only freelancers (handle both uppercase and lowercase)
      const freelancers = allUsers.filter((user: User) => 
        user.role === 'freelancer' || (user.role as string) === 'FREELANCER'
      );
      
      console.log('🔍 Filtered freelancers:', freelancers);
      
      return freelancers;
    } catch (error) {
      console.error('❌ FreelancerService: Error getting all freelancers:', error);
      throw error;
    }
  }
}

const freelancerService = new FreelancerService();
export default freelancerService;