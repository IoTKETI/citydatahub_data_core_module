/*
 *
 * OpenMakers version 2.0.0
 *
 *  Copyright ⓒ 2021 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file except in
 *  compliance with license agreement with kt corp. Any redistribution or use of this
 *  software, with or without modification shall be strictly prohibited without prior written
 *  approval of kt corp, and the copyright notice above does not evidence any actual or
 *  intended publication of such software.
 *
 */

import Vue from 'vue'
import VueI18n from 'vue-i18n'
import { getCookie } from './cookieParser'

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
  locale: getCookie('langCd') || 'en',
  messages: loadLocaleMessages()
})
