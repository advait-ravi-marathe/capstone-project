(function(){
  var root=document.documentElement;
  var saved=null;try{saved=localStorage.getItem('coverstory-theme');}catch(e){}
  var media=window.matchMedia('(prefers-color-scheme: dark)');
  root.dataset.theme=saved==='light'||saved==='dark'?saved:(media.matches?'dark':'light');
  function sync(){document.querySelectorAll('[data-theme-toggle]').forEach(function(b){var dark=root.dataset.theme==='dark';b.textContent=dark?'Dark mode: on':'Dark mode: off';b.setAttribute('aria-pressed',String(dark));b.setAttribute('aria-label','Dark mode');});}
  document.addEventListener('DOMContentLoaded',function(){sync();document.querySelectorAll('[data-theme-toggle]').forEach(function(b){b.addEventListener('click',function(){root.dataset.theme=root.dataset.theme==='dark'?'light':'dark';saved=root.dataset.theme;try{localStorage.setItem('coverstory-theme',saved);}catch(e){}sync();});});});
  if(media.addEventListener)media.addEventListener('change',function(e){if(!saved){root.dataset.theme=e.matches?'dark':'light';sync();}});
})();
