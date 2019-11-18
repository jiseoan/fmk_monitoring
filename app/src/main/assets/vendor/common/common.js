// common.js
//
// Last Updated: -
// written by Jae Su Kim
function parseResult(result)
{
  var resultArray;
  try 
  {
    resultArray = $.parseJSON(result);
  }
  catch (e)
  {
    resultArray = result;
  }
  var resultObj = {};
  resultObj.code = resultArray['result_code'];
  resultObj.data = resultArray['result_data'];
  return resultObj;
}

function dbSql(query) {
  var result = null;
  console.log(query);
  var checkOS = getPlatform();
  try {
    if (checkOS == "Android") {
      result = window.android.DBSQL(query);
    } else {
      console.log("ignore dbSql(" + query + ")");
    }
  } catch (e) { 
    console.log("ignore dbSql(" + query + ")");
  }
  return result;
}
function dbMerge(table, param) {
  var result = null;
  if (param) {
    var fields = null;
    var values = null;

    for (var key in param) {     
      if (fields === null) {
        fields = key;
        values = (param[key] === null)?"NULL":"'" + param[key] + "'";
      } else {
        fields += ", " + key;
        values += (param[key] === null)?", NULL":", '" + param[key] + "'";
      }
    };

    result = dbSql("insert or replace into " + table + " (" + fields + ") values (" + values + ")");
  }
  return result;
}

function dbSelect(field, table, where)
{
  result = dbSql("SELECT " + field + " FROM " + table + " WHERE " + where);
  return result;
}