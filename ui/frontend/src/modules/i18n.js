import Vue from 'vue'
import VueI18n from 'vue-i18n'

Vue.use(VueI18n)

function loadLocaleMessages () {
  const locales = require.context('@/assets/locales', true, /[A-Za-z0-9-_,\s]+\.json$/i)
  const messages = {}
  locales.keys().forEach(key => {
    const matched = key.match(/([A-Za-z0-9-_]+)\./i)
    if (matched && matched.length > 1) {
      const locale = matched[1]
      messages[locale] = locales(key)
    }
  })
  return messages
}

export default new VueI18n({
  locale: getCookie('langCd'), // localStorage sync to call defaultLocale()
  messages: loadLocaleMessages()
})

function defaultLocale() {
  let locale = localStorage.getItem('langCd');
  if (!locale) {
    locale = 'en';
  }
  return locale;
}

function getCookie(cName) {
  cName = cName + '=';
  let cookieData = document.cookie;
  let start = cookieData.indexOf(cName);
  let cValue = '';
  if(start !== -1){
    start += cName.length;
    let end = cookieData.indexOf(';', start);
    if(end === -1)end = cookieData.length;
    cValue = cookieData.substring(start, end);
  }
  return unescape(cValue);
}
