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

function getPlatform()
{
  var platform = "Unknown";
  if (/android/i.test(navigator.userAgent)) {
    platform = "Android";
  }
  if (/iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream) {
    platform = "iOS";
  }
  return platform;
}

function callNative(command, param) {
  var result  = null;
  if (typeof(param) === "undefined") param = "";
  var checkOS = getPlatform();
  try {
    if (checkOS == "Android") {
      if (command == "deviceUniq") {
        // 디바이스 고유값 가져오기
        result = window.android.deviceUniq();
      } else if (command == "bottomActionBar") {
        // 하단바 레이아웃별 보이기
        // bottom_actionbar_A1 : 작업시작 (QR코드스킨 열기)
        // bottom_actionbar_A2 : 작업시작 (카메라 열기 - 모니터링용) | 취소
        // bottom_actionbar_A3 : 재촬영 | AS요청 | 확인
        // bottom_actionbar_B1 : 목록
        // bottom_actionbar_B2 : 등록 | 취소
        // bottom_actionbar_C1 : AS처리 재등록
        // bottom_actionbar_C2 : 재촬영 | 확인
        // bottom_actionbar_D1 : AS처리등록
        // bottom_actionbar_D2 : 완료 | 취소

        window.android.bottomActionBar(param, true);
      } else if (command == "toolBar") {
        // 상단바 레이아웃별 보이기
        // top_actionbar_A1 : 메인 상단바 열기
        window.android.toolBar(param, true);
      }
    } else {
      console.log("ignore callNative('" + command + "', '" + param + "')");
    }
  } catch (e) { 
    console.log("ignore callNative('" + command + "', '" + param + "')");
  }

  return result;
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

    result = dbSql("INSERT OR REPLACE INTO " + table + " (" + fields + ") VALUES (" + values + ")");
  }
  return result;
}

function dbSelect(field, table, where, orderBy)
{
  if (typeof(where) === "undefined") where = "";
  if (typeof(orderBy) === "undefined") orderBy = "";
  var query = "SELECT " + field + " FROM " + table;
  if (where) query += " WHERE " + where;
  if (orderBy) query += " ORDER BY " + orderBy;
  result = dbSql(query);
  return result;
}

var __ajaxSkinList = {};

function addAjaxSkin(skin_dom, replace_text)
{
  var domHtml = $(skin_dom).html();
  if (typeof(__ajaxSkinList[skin_dom]) === "undefined") // 등록되어 있지 않은 템플릿이면 넣어줌
  {
    __ajaxSkinList[skin_dom] = domHtml;
    if (typeof(replace_text) !== "undefined")
    {
      $(skin_dom).html(replace_text);
    }
  }
  else
  {
    domHtml = getAjaxSkin(skin_dom);
  }
  return domHtml;
}


function getAjaxSkin(skin_dom)
{
  if (typeof(__ajaxSkinList[skin_dom]) !== "undefined")
  {
    return __ajaxSkinList[skin_dom];
  }
  return "";
}

function replaceDom(skin, param, delimiter)
{
  //console.log("replaceDom " + skin);
  if (typeof(delimiter) === "undefined")
    delimiter = "[]";
  var newSkin = "";
  //console.log(param);
  if (param != null && typeof(param) === "object") // 2차원 배열-오브젝트
  {
    if (param.length > 0)
    {
      if (typeof(param[0]) === "object")
      {
        for (var key in param) 
        {
          var replacedText = skin;
          for (var key2 in param[key])
          {
            //replacedText = replaceAll(replacedText, delimiter.substr(0, 1) + key2 + delimiter.substr(1, 1), param[key][key2]);
            var regEx = new RegExp("\\" + delimiter.substr(0, 1) + key2 + "\\" + delimiter.substr(1, 1), "g");
            replacedText = String(replacedText).replace(regEx, param[key][key2]);
          }
          
          replacedText = String(replacedText).replace(/tag=/g, 'src=');

          newSkin += replacedText;
        }
      }
    } 
    else // 1차원 오브젝트
    {
      newSkin = skin;
      if (typeof(param.length) === "undefined" && typeof(param) === "object") // 배열이 아닌 오브젝트라면
      {
        for (var key in param) 
        {
          // newSkin = replaceAll(newSkin, delimiter.substr(0, 1) + key + delimiter.substr(1, 1), param[key]);
          var regEx = new RegExp("\\" + delimiter.substr(0, 1) + key + "\\" + delimiter.substr(1, 1), "g");
          newSkin = String(newSkin).replace(regEx, param[key]);
        }              
        newSkin = String(newSkin).replace(/tag=/g, 'src=');
      }
      else // 배열이면서 length가 0일 경우
      {
        newSkin = "";
      }
    }   
  }

  return newSkin;
}