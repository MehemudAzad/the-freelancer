// Test script for semantic search functionality
// This script can be run in the browser console to test the semantic search API

const testSemanticSearch = async () => {
  console.log('🧪 Testing Semantic Search API...');
  
  const baseURL = 'http://localhost:8080';
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    console.error('❌ No access token found. Please login first.');
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };

  // Test queries based on the images shown
  const testQueries = ['mongodb', 'react', 'database optimization', 'frontend frameworks'];

  for (const query of testQueries) {
    try {
      console.log(`\n🔍 Testing query: "${query}"`);
      
      const response = await fetch(`${baseURL}/api/gigs/search/semantic?q=${encodeURIComponent(query)}`, {
        method: 'GET',
        headers: headers
      });

      if (response.ok) {
        const data = await response.json();
        console.log(`✅ Success for "${query}":`, data);
        console.log(`   📊 Found ${Array.isArray(data) ? data.length : 'unknown'} results`);
        
        if (Array.isArray(data) && data.length > 0) {
          console.log(`   📝 First result:`, {
            id: data[0].id,
            title: data[0].title,
            category: data[0].category
          });
        }
      } else {
        console.error(`❌ Error for "${query}":`, response.status, response.statusText);
        const errorText = await response.text();
        console.error('   Error details:', errorText);
      }
    } catch (error) {
      console.error(`❌ Network error for "${query}":`, error);
    }
  }
  
  console.log('\n🏁 Semantic search testing completed!');
};

// Export the test function for use in browser console
window.testSemanticSearch = testSemanticSearch;

console.log('🔧 Semantic search test utility loaded!');
console.log('📝 Run testSemanticSearch() in the console to test the API');