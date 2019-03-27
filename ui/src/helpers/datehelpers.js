
function twoDigit(x) {

  let y = "" + x;
  if (y.length<2) {

      return "0" +y;
  }
  return y;

}


function convertTS(ts) {

  if (ts==null || ts==0) {
    return "";

  }

  var date = new Date(ts);


  return date.getFullYear() + "-" + twoDigit(date.getMonth()+1) + "-" + twoDigit(date.getDate()) +" " + twoDigit(date.getHours()) +":" + twoDigit(date.getMinutes()) +":" + twoDigit(date.getSeconds());
}


export {
  convertTS
};