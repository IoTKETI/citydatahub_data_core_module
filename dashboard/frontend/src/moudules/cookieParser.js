export const getCookie = (name) => {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const splited = cookie.split('=');
    if (splited[0] === name) {
      return splited[1];
    }
  }
}
