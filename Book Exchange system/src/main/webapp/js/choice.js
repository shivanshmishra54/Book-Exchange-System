document.addEventListener('DOMContentLoaded', () => {
           const grids = [
               document.getElementById('topicGrid'),
               document.getElementById('typeGrid'),
               document.getElementById('languageGrid')
           ];

           
           const handleSelection = (event) => {
               const item = event.target;
               if (item.classList.contains('topic-item')) {
                   item.classList.toggle('selected');
               }
           };
           
           grids.forEach(grid => {
               grid.addEventListener('click', handleSelection);
           });
       });

       
       function collectAndSubmit() {
           const selectedItems = document.querySelectorAll('.topic-item.selected');
           const hiddenInput = document.getElementById('userPreferences');
           const preferenceForm = document.getElementById('preferenceForm');
           
           if (selectedItems.length === 0) {
               alert("Please select at least one preference before proceeding!");
               return;
           }
           
           
           const preferences = {
               topics: [],
               types: [],
               languages: []
           };
           
           selectedItems.forEach(item => {
               const type = item.getAttribute('data-type');
               const value = item.getAttribute('data-value');
               
               if (preferences[type]) {
                   preferences[type].push(value);
               }
           });
           
          
           const preferencesJSON = JSON.stringify(preferences);
           
           
           hiddenInput.value = preferencesJSON;
           
          
           console.log("JSON Data Sent to Server:", preferencesJSON);
           
           
           preferenceForm.submit();
       }