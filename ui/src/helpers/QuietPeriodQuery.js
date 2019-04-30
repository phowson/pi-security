
class QuietPeriod {
    constructor() {
      this.dayOfWeekStart = -1;
      this.hourOfDayStart = -1;
  
      this.dayOfWeekEnd = -1;
      this.hourOfDayEnd = -1;
      this.durationHours = 0;
  
  
    }
  
  }
  
  
  class QuietPeriodQuery {
  
  
    getQuietPeriods(query2, callback) {
      
      query2.on("value", function (snapshot) {
  
        var perDayOfWeekTable = new Map();
        var allDays = new Map();
        for (var j = 0; j < 24; ++j) {
          allDays.set(j, 0);
        }


        for (var i = 0; i < 7; ++i) {
          var m = new Map();
          for (var j = 0; j < 24; ++j) {
            m.set(j, 0);
          }
  
          perDayOfWeekTable.set(i, m);
        }
  
  


        snapshot.forEach(function (childSnapshot) {
          if ("ACTIVITY" == childSnapshot.child("type").val()) {
            var d = new Date(childSnapshot.child("timestamp").val());
            var dl = perDayOfWeekTable.get(d.getDay());
            var v = dl.get(d.getHours());
            dl.set(d.getHours(), v + 1);

            v = allDays.get(d.getHours());
            allDays.set(d.getHours(), v + 1);

          }
          return false;
        });
  
  
        var periods = [];
  
        var currentQuietPeriod = null;
        for (var i = 0; i < 7; ++i) {
          var currentDay = perDayOfWeekTable.get(i);
          for (var j = 0; j < 24; ++j) {
            var currentAct = currentDay.get(j);
            if (currentAct == 0) {
              if (currentQuietPeriod == null) {
                currentQuietPeriod = new QuietPeriod();
                currentQuietPeriod.dayOfWeekStart = i;
                currentQuietPeriod.hourOfDayStart = j;
              } else {
                currentQuietPeriod.dayOfWeekEnd = i;
                currentQuietPeriod.hourOfDayEnd = j;
                ++currentQuietPeriod.durationHours;
              }
            } else {
              if (currentQuietPeriod != null) {
                periods.push(currentQuietPeriod);
                currentQuietPeriod = null;
              }
  
            }
          }
        }
  
        if (currentQuietPeriod != null) {
          periods.push(currentQuietPeriod);
        }

        var periods2=[];
        currentQuietPeriod = null;
        for (var j = 0; j < 24; ++j) {
          var currentAct = allDays.get(j);
          if (currentAct == 0) {
            if (currentQuietPeriod == null) {
              currentQuietPeriod = new QuietPeriod();
              currentQuietPeriod.dayOfWeekStart = i;
              currentQuietPeriod.hourOfDayStart = j;
            } else {
              currentQuietPeriod.dayOfWeekEnd = i;
              currentQuietPeriod.hourOfDayEnd = j;
              ++currentQuietPeriod.durationHours;
            }
          } else {
            if (currentQuietPeriod != null) {
              periods2.push(currentQuietPeriod);
              currentQuietPeriod = null;
            }

          }
        }
        if (currentQuietPeriod != null) {
          periods2.push(currentQuietPeriod);
        }
  
  
        callback(periods, periods2);
  
      });
  
  
  
    }
  
  
  }
  
  export { QuietPeriod, QuietPeriodQuery};
