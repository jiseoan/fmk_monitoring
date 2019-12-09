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

function callNative(command, param, param2) {
  var result  = null;
  if (typeof(param) === "undefined") param = "";
  if (typeof(param2) === "undefined") param2 = "";
  var checkOS = getPlatform();
  try {
    if (checkOS == "Android") {
      console.log("[INFO] callNative('" + command + "', '" + param + "', '" + param2 + "')");
      if (command == "hideToolBar") {
        // 상단바 안보이기
        result = window.android.hideToolBar();
      } else if (command == "hideBottomActionBar") {
        // 하단바 안보이기
        result = window.android.hideBottomActionBar();
      } else if (command == "deviceUniq") {
        // 디바이스 고유값 가져오기
        result = window.android.deviceUniq();
      } else if (command == "bottomActionBar") {
        // 하단바 레이아웃별 보이기
        // jobStart : 작업시작 (QR코드스킨 열기)
        // jobStartAndCancel : 작업시작 (카메라 열기 - 모니터링용) | 취소
        // retakeAndAsAndConfirm : 재촬영 | AS요청 | 확인
        // list : 목록
        // insertAndCancel : 등록 | 취소
        // reAsProcessing : AS처리 재등록
        // retakeAndConfirm : 재촬영 | 확인 <- ad: 광고재촬영, building: 단지 대표 이미지 재촬영
        // asProcessing : AS처리등록
        // completeAndCancel : 완료 | 취소
        window.android.bottomActionBar(param, param2);
      } else if (command == "toolBar") {
        // 상단바 레이아웃별 보이기
        // main : 뒤로가기 | 타이틀명 | 홈
        // popup : 타이틀명 | 닫기
        window.android.toolBar(param, param2);
      } else if (command == "setTitle") {
        // 타이틀명 변경하기
        window.android.toolBar(param);
      } else if (command == "newWebView") {
        // 새로운 웹뷰 보이기
        window.android.newWebView(param);
      } else if (command == "camera") {
        // 카메라 촬영 보이기
        window.android.camera(param, JSON.stringify(param2));
      } else if (command == "massQueries") {
        window.android.massQueries(JSON.stringify(param));
      }
    } else {
      console.log("ignore callNative('" + command + "', '" + param + "', '" + param2 + "')");
    }
  } catch (e) { 
    console.log("ignore callNative('" + command + "', '" + param + "', '" + param2 + "')");
  }

  return result;
}

function NativeCallback(command, param, result)
{
  if (typeof(param) === "undefined") param = "";
  if (typeof(result) === "undefined") result = "";
  var checkOS = getPlatform();
  try {
    if (checkOS == "Android") {
      console.log("[INFO] NativeCallback: " + command + ", " + param+ ", " + result);
      if (command == "back")
      {
        // 뒤로가기
        history.back(-1);
      }
      else if (command == "home")
      {
        // 홈
        location.href = "main.html";
      }
      else if (command == "close")
      {
        // 닫기
        window.android.webViewClose();
      }
      else if (command == "qrScan")
      {
        // qr 스케너
        window.android.qrCode();
      }
      else if (command == "qrScanResult")
      {
        // qr 스케너 결과
        var qrCode = result;
        if (typeof window["qrScanResult"] === "function") {
          qrScanResult(qrCode);
        } else {
          console.log("no function qrScanResult()");
        }
      }
      else if (command == "monitoringCamera" || command == "reMonitoringCamera" || command == "reBuildingCamera" || command == "reAdCamera")
      {
        // 모니터링 촬영, 모니터링 재촬영, 단지 대표이미지 재촬영, 광고 재촬영
        var param = "";
        var param2 = "";

        if (typeof window["paramData"] === "function") {
          param2 = paramData();
        }

        if(command == "monitoringCamera" || command == "reMonitoringCamera") param = "monitoring";
        if (command == "reBuildingCamera") param = "building";
        if (command == "reAdCamera") param = "ad";
        window.android.camera(param, JSON.stringify(param2));
      }
      else if (command == "cameraResult")
      {
        // 카메라 촬영 결과
        if (typeof window["cameraResult"] === "function") {
          cameraResult(param, result);
        } else {
          console.log("no function cameraResult()");
        }
      }
      else if (command == "cancel")
      {
        // 취소
        history.back(-1);
      }
      else if (command == "asRequest")
      {
        // as요청
        // post로 asRequest.html 페이지로 데이터값을 보냄
        if (typeof window["asRequest"] === "function") {
          asRequest();
        } else {
          console.log("no function asRequest()");
        }
      }
      else if (command == "confirm")
      {
        // 확인
        // 데이터를 저장하는 프로세스 진행 <- 네이티브로 값을 보내어 서버로 저장되도록 해야 함
        if (typeof window["dataSaveProcess"] === "function") {
          dataSaveProcess();
        } else {
          console.log("no function dataSaveProcess()");
        }
      }
      else if (command == "registration")
      {
        // 등록
        // 데이터를 저장하는 프로세스 진행
        if (typeof window["dataSaveProcess"] === "function") {
          dataSaveProcess();
        } else {
          console.log("no function dataSaveProcess()");
        }
      }
      else if (command == "list")
      {
        // 목록
        history.back(-1);
      }
      else if (command == "asProcessingRegistration" || command == "asProcessingReRegistration")
      {
        // as처리 등록, as처리 재등록
        // asProcessingEdit.html 로 이동
        if (typeof window["asProcessingRegistration"] === "function") {
          asProcessingRegistration();
        } else {
          console.log("no function asProcessingRegistration()");
        }
      }
      else if (command == "complete")
      { 
        // 데이터를 저장하는 프로세스 진행
      }
      else if (command == "completeQueries")
      { 
        // massQueries 로 데이터를 저장후 콜백
        if (typeof window["completeQueries"] === "function") {
          completeQueries();
        } else {
          console.log("no function completeQueries()");
        }
      }
    } else {
      console.log("[INFO] NativeCallback: " + command + ", " + param+ ", " + result);
    }
  } catch (e) { 
    console.log("[INFO] NativeCallback: " + command + ", " + param+ ", " + result);
  }
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

function dbInsert(table, param) {
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

// 파라이터값 가져오기
$.urlParam = function(name, url){
  if (typeof(url) === "undefined") url = window.location.href;
  var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(url);
  if (results==null){
    return null;
  }
  else{
    return results[1] || null;
  }
}

//아래 함수를 적절한 공용 라이브러리로 이동해주세요.
function stringFormat() {
  var expression = arguments[0]; 
  for (var i=1; i < arguments.length; i++) {
    var prttern = "{" + (i - 1) + "}"
    expression = expression.replace(prttern, arguments[i]);
  }

  return expression;
}

var BASE_URL = "https://dev.treeter.net/fmksystem";