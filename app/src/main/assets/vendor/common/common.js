// common.js
//
// Last Updated: -
// written by Jae Su Kim
"use strict";
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

function callNative(command, param, param2, param3) {
  var result  = null;
  if (typeof(param) === "undefined") param = "";
  if (typeof(param2) === "undefined") param2 = "";
  if (typeof(param3) === "undefined") param3 = "";
  var checkOS = getPlatform();
  try {
    if (checkOS == "Android") {
      console.log("[INFO] callNative('" + command + "', '" + JSON.stringify(param) + "', '" + JSON.stringify(param2) + "', '" + JSON.stringify(param3) + "')");
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
        // normal : 타이틀명
        window.android.toolBar(param, param2);
      } else if (command == "setTitle") {
        // 타이틀명 변경하기
        window.android.setTitle(param);
      } else if (command == "newWebView") {
        // 새로운 웹뷰 보이기
        window.android.newWebView(param);
      } else if (command == "camera") {
        // 카메라 촬영 보이기
        window.android.camera(param, param2, JSON.stringify(param3));
      } else if (command == "qrScan") {
        // qr 스케너 보이기
        window.android.qrCode();
      } else if (command == "massQueries") {
        window.android.massQueries(JSON.stringify(param));
      } else if (command == "toastMessage") {
        // 토스트 알림 띄우기
        window.android.toastMessage(param);
      } else if (command == "downloadMedia") {
        // 동영상/이미지 보기화면 보이기
        window.android.downloadMedia(param, param2, param3);
      } else if (command == "uploadData") {
        // 서버로 업로드
       window.android.uploadData(JSON.stringify(param));
      }
    } else {
      console.log("ignore callNative('" + command + "', '" + JSON.stringify(param) + "', '" + JSON.stringify(param2) + "', '" + JSON.stringify(param3) + "')");
    }
  } catch (e) { 
    console.log("ignore callNative('" + command + "', '" + JSON.stringify(param) + "', '" + JSON.stringify(param2) + "', '" + JSON.stringify(param3) + "')");
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
      console.log("[INFO] NativeCallback: " + command + ", " + JSON.stringify(param)+ ", " + JSON.stringify(result));
      if (command == "back")
      {
        // 뒤로가기
        history.back(-1);
      }
      else if (command == "home")
      {
        // 홈
        if (typeof window["home"] === "function") {
          home();
        } else {
          location.href = "main.html";
        }
      }
      else if (command == "close")
      {
        // 닫기
        window.android.closeWebView();
      }
      else if (command == "login")
      {
        // 로그인
       if (typeof window["login"] === "function") {
          login();
        } else {
          console.log("no function login()");
        }
      }
      else if (command == "request")
      {
        // 로그인
       if (typeof window["request"] === "function") {
          request();
        } else {
          console.log("no function request()");
        }
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
        var param3 = "";
        console.log("1");
        if (typeof window["paramData"] === "function") {
          console.log("2");
          var addParam = paramData();
          console.log(addParam);
          param2 = addParam[0];
          param3 = addParam[1];
        } else {
          console.log("no function paramData()");
        }
        console.log("3");

        if(command == "monitoringCamera" || command == "reMonitoringCamera") param = "monitoring";
        if (command == "reBuildingCamera") param = "building";
        if (command == "reAdCamera") param = "ad";

        console.log("ignore callNative('" + command + "', '" + JSON.stringify(param) + "', '" + JSON.stringify(param2) + "', '" + JSON.stringify(param3) + "')");
        window.android.camera(param, param2, JSON.stringify(param3));
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
      else if (command == "jobStart")
      {
        // 작업시작
        if (typeof window["jobStart"] === "function") {
          jobStart();
        } else {
          console.log("no function jobStart()");
        }
      }
      else if (command == "cancel")
      {
        // 취소
        if (typeof window["cancel"] === "function") {
          cancel();
        } else {
          history.back(-1);
        }
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
        } else if (typeof window["confirm"] === "function") {
          confirm();
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
      else if (command == "uploadData")
      { 
        // 데이터를 저장하는 프로세스 진행 - upload 페이지 결과가 뿌려짐
        console.log(result, param);
        if (typeof window["uploadDataCallBack"] === "function") {
          uploadDataCallBack(result, param);
        } else {
          console.log("no function uploadDataCallBack()");
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

    result = dbSql("INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ")");
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
  var result = dbSql(query);
  return $.parseJSON(result);
}

function dbRowArray(query)
{
  var row = dbSql(query);
  console.log(row);
  if (row === null) return null;
  row = $.parseJSON(row);
  row = (row.length > 0)?row[0]:null;
  return row;
}

function dbResultArray(query)
{
  var result = dbSql(query);
  console.log(result);
  if (result === null) return null;
  result = $.parseJSON(result);
  result = (result.length > 0)?result:null;
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

var BASE_URL = "http://dev.treeter.net/fmksystem";



var getRemoteVersionData = null;

function dataSync(agentCode, tost) {
  if (typeof(tost) === "undefined") tost = true;
  // 서버에 업로드 되지않은 데이터를 업로드 한다.
  // 업로드가 완료되면 내려받기 위해 버전을 체크한다.
  if(tost) callNative("toastMessage", "동기화중 ...");    
    if (typeof window["listLoadingStart"] === "function") {
      listLoadingStart();
    }
  getRemoteVersion(agentCode, tost);
}

function getNowDate() {
  var currentDay = new Date();  
  var nyyyy = currentDay.getFullYear();
  var nmm = Number(currentDay.getMonth()) + 1;
  var ndd = currentDay.getDate();
  var nowDate = nyyyy + "-" + (String(nmm).length === 1 ? '0' + nmm : nmm) + "-" + (String(ndd).length === 1 ? '0' + ndd : ndd);
  return nowDate;
}

function getThisWeekDates() {
  var currentDay = new Date();
  var theYear = currentDay.getFullYear();
  var theMonth = currentDay.getMonth();
  var theDate  = currentDay.getDate();
  var theDayOfWeek = currentDay.getDay();
  var thisWeekDates = [];
  for(var i=0; i<7; i++) {
    if (i == 0 || i == 6) {
      var resultDay = new Date(theYear, theMonth, theDate + i);
      var yyyy = resultDay.getFullYear();
      var mm = Number(resultDay.getMonth()) + 1;
      var dd = resultDay.getDate();
     
      mm = String(mm).length === 1 ? '0' + mm : mm;
      dd = String(dd).length === 1 ? '0' + dd : dd;
      var ymd = yyyy + '-' + mm + '-' + dd;
      if (i == 0) thisWeekDates['start'] = ymd;
      if (i == 6) thisWeekDates['end'] = ymd;
    }
  }
  return thisWeekDates;
}

function getRemoteVersion(agentCode, tost) {
  if (typeof(tost) === "undefined") tost = true;
  var thisWeekDates = getThisWeekDates();
  $.post(BASE_URL + '/Bmm_api/getRemoteVersion', {
    agent_code: agentCode,
    start_date: thisWeekDates['start'],
    end_date: thisWeekDates['end']
  })
  .done(function(data) {
    var result = parseResult(data);
    //console.log(result.data);
    if (result.code == "success") {
      // 각 데이터의 버전을 체크한다.
      getRemoteVersionData = result.data;

      var getAgentData = dbSelect("*", "agent", "agent_code='" + agentCode + "'");
      var agentType = getAgentData[0]['agent_type'];

      var dataNamesAry = ['notice','building','monitoring_request','ad_check_request','processing','code','as_request','as_processing'];
      var dataNames = '';
      var versionData = dbRowArray("SELECT * FROM version");
      if (versionData && (versionData['ver_date'] == getNowDate()) && (versionData['agent_code'] == agentCode)) {
        $.each(dataNamesAry, function (idx, el) {
          console.log(getRemoteVersionData[el], versionData[el]);
          if (getRemoteVersionData[el] != versionData[el]) {
            if (agentType == 'A') {
              if (el == 'building' || el == 'code' || el == 'as_request' || el == 'as_processing') {
                dataNames += (dataNames)?"," + el:el;
              }
            } else {
              dataNames += (dataNames)?"," + el:el;
            }
          }      
        });
      } else {
        dataNames = 'notice,building,monitoring_request,ad_check_request,processing,code,as_request,as_processing';
      }

      getRemoteVersionData['agent_code'] = agentCode;
      console.log(dataNames);

      //var dataNames = 'notice,building,monitoring_request,ad_check_request,processing,code,as_request,as_processing';
      if (dataNames == "") {
        completeQueries();
      } else {
        getDownloadData(agentCode, dataNames);
      }
    } else {
      //$.alert(result.data);
      if(tost) callNative("toastMessage", result.data);
      if (typeof window["listLoadingEnd"] === "function") {
        listLoadingEnd();
      }
    }
  })
  .fail(function() {
    console.log('failed');
    if(tost) callNative("toastMessage", "동기화에 실패하였습니다.\n인터넷연결상태를 확인해주세요.");    
    if (typeof window["listLoadingEnd"] === "function") {
      listLoadingEnd();
    }
  });
}

function dataBuildingSync(agentCode, tost, params) {
  if (typeof(tost) === "undefined") tost = true;
  if(tost) callNative("toastMessage", "동기화중 ...");
  getDownloadData(agentCode, "building", tost, params);
}

function getDownloadData(agentCode, dataNames, tost, params) {
  if (typeof(tost) === "undefined") tost = true;
  if (typeof(params) === "undefined") params = {};
  var thisWeekDates = getThisWeekDates();
  $.post(BASE_URL + '/Bmm_api/getUpdateDataList', {
    agent_code: agentCode,
    start_date: thisWeekDates['start'],
    end_date: thisWeekDates['end'],
    dataNames: dataNames,
    params: params
  })
  .done(function(data) {
    var result = parseResult(data);
    console.log(result.data);
    if (result.code == "success") {
    
      let queryList = [];
      let resData = result.data;
      
      // 공지사항
      if (resData.hasOwnProperty('notice')) {
        queryList.push({"query": "delete from notice"});
        for(var i=0;i<result.data.notice.length;++i) {
          let currentData = result.data.notice[i];
          queryList.push({"query": stringFormat("insert into notice (notice_id, notice_type, title, content, create_date) " + "values('{0}', '{1}', '{2}', '{3}', '{4}')", currentData.notice_id, currentData.notice_type, currentData.title, currentData.content, currentData.create_date)});
        }
      }

      // 단지
      if (resData.hasOwnProperty('building')) {
        var mode = (params.hasOwnProperty('building_id'))?"update":"insert";
        if (mode == "insert"){
          queryList.push({"query": "delete from building"});
        }
        for(var i=0;i<result.data.building.length;++i) {
          let currentData = result.data.building[i];
          if (mode == "insert"){
            queryList.push({"query": stringFormat("insert into building (building_id, building_name, machine_cnt, address) " + "values('{0}', '{1}', '{2}', '{3}')", currentData.building_id, currentData.building_name, currentData.machine_cnt, currentData.address)});
          } else {
            queryList.push({"query": stringFormat("update building set building_name='{1}', machine_cnt='{2}', address='{3}' where building_id='{0}'", currentData.building_id, currentData.building_name, currentData.machine_cnt, currentData.address)});
          }
        }
      }

      // 단지 매체
      if (resData.hasOwnProperty('building_locate')) {
        var mode = (params.hasOwnProperty('building_id'))?"update":"insert";
        if (mode == "insert"){
          queryList.push({"query": "delete from building_locate"});
        }
        for(var i=0;i<result.data.building_locate.length;++i) {
          let currentData = result.data.building_locate[i];
          if (mode == "insert"){
            queryList.push({"query": stringFormat("insert into building_locate (building_locate_id, building_id, dong, unit_name, machine_code, qr_serial_code) " + "values('{0}', '{1}', '{2}', '{3}', '{4}', '{5}')", currentData.building_locate_id, currentData.building_id, currentData.dong, currentData.unit_name, currentData.machine_code, currentData.qr_serial_code)});
          } else {
            queryList.push({"query": stringFormat("update building_locate set building_id='{1}', dong='{2}', unit_name='{3}', machine_code='{4}', qr_serial_code='{5}' where building_locate_id='{0}'", currentData.building_locate_id, currentData.building_id, currentData.dong, currentData.unit_name, currentData.machine_code, currentData.qr_serial_code)});
          }
        }
      }

      // 모니터링 요청
      if (resData.hasOwnProperty('monitoring_request')) {
        queryList.push({"query": "delete from monitoring_request"});
        for(var i=0;i<result.data.monitoring_request.length;++i) {
          let currentData = result.data.monitoring_request[i];
          queryList.push({"query": stringFormat("insert into monitoring_request (monitoring_request_id, building_id, machine_cnt, building_locate_ids, request_date, processing_flag, processing_date) " + "values('{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}')", currentData.monitoring_request_id, currentData.building_id, currentData.machine_cnt, currentData.building_locate_ids, currentData.request_date, currentData.processing_flag, currentData.processing_date)});
        }
      }

      // 광고게첨 요청
      if (resData.hasOwnProperty('ad_check_request')) {
        queryList.push({"query": "delete from ad_check_request"});
        for(var i=0;i<result.data.ad_check_request.length;++i) {
          let currentData = result.data.ad_check_request[i];
          queryList.push({"query": stringFormat("insert into ad_check_request (ad_check_request_id, ad_name, ad_type, ad_url, request_date, ad_check_building_id, building_id, building_file_url, processing_flag, return_code_id, return_desc) " + "values('{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}', '{7}', '{8}', '{9}', '{10}')", currentData.ad_check_request_id, currentData.ad_name, currentData.ad_type, currentData.ad_url, currentData.request_date, currentData.ad_check_building_id, currentData.building_id, currentData.building_file_url, currentData.processing_flag, currentData.return_code_id, currentData.return_desc)});
        }
      }

      // 모니터링/광고게첨 처리
      if (resData.hasOwnProperty('processing')) {
        queryList.push({"query": "delete from processing"});
        for(var i=0;i<result.data.processing.length;++i) {
          let currentData = result.data.processing[i];
          queryList.push({"query": stringFormat("insert into processing (building_id, building_locate_id, machine_code, processing_file_url, processing_date, qr_flag, no_qr_type_code_id, no_qr_desc, monitoring_request_id, ad_check_building_id, processing_id) " + "values('{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}', '{7}', '{8}', '{9}', '{10}')", currentData.building_id, currentData.building_locate_id, currentData.machine_code, currentData.processing_file_url, currentData.processing_date, currentData.qr_flag, currentData.no_qr_type_code_id, currentData.no_qr_desc, currentData.monitoring_request_id, currentData.ad_check_building_id, currentData.processing_id)});
        }
      }

      // 모니터링 전체 사용 코드
      if (resData.hasOwnProperty('code')) {
        queryList.push({"query": "delete from code"});
        for(var i=0;i<result.data.code.length;++i) {
          let currentData = result.data.code[i];
          queryList.push({"query": stringFormat("insert into code (code_id, parent_id, code, codename) " + "values('{0}', '{1}', '{2}', '{3}')", currentData.code_id, currentData.parent_id, currentData.code, currentData.codename)});
        }
      }

      // AS 요청
      if (resData.hasOwnProperty('as_request')) {
        queryList.push({"query": "delete from as_request"});
        for(var i=0;i<result.data.as_request.length;++i) {
          let currentData = result.data.as_request[i];
          queryList.push({"query": stringFormat("insert into as_request (building_id, building_locate_id, machine_code, request_date, request_type_code_ids, request_desc, processing_flag, as_request_id) " + "values('{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}', '{7}')", currentData.building_id, currentData.building_locate_id, currentData.machine_code, currentData.request_date, currentData.request_type_code_ids, currentData.request_desc, currentData.processing_flag, currentData.as_request_id)});
        }
      }

      // AS 처리
      if (resData.hasOwnProperty('as_processing')) {
        queryList.push({"query": "delete from as_processing"});
        for(var i=0;i<result.data.as_processing.length;++i) {
          let currentData = result.data.as_processing[i];
          queryList.push({"query": stringFormat("insert into as_processing (as_request_id, processing_type_code_id, processing_desc, processing_date, as_processing_id) " + "values('{0}', '{1}', '{2}', '{3}', '{4}')", currentData.as_request_id, currentData.processing_type_code_id, currentData.processing_desc, currentData.processing_date, currentData.as_processing_id)});
        }
      }

      console.log("encoded.... end");
      callNative('massQueries', queryList);
    } else {
      if(tost) callNative("toastMessage", "동기화에 실패하였습니다.");    
      if (typeof window["listLoadingEnd"] === "function") {
        listLoadingEnd();
      }
    }
  })
  .fail(function() {
    console.log('failed');
    if(tost) callNative("toastMessage", "동기화에 실패하였습니다.\n인터넷연결상태를 확인해주세요.");    
    if (typeof window["listLoadingEnd"] === "function") {
      listLoadingEnd();
    }
  });
}

// 다운로드 완료시 콜백
function completeQueries() {
  if (getRemoteVersionData) {
    // version 테이블의 정보를 업데이트 한다.
    console.log(getRemoteVersionData);
    getRemoteVersionData['ver_date'] = getNowDate();
    dbSql("DELETE FROM version");
    dbInsert("version", getRemoteVersionData);
  }

  if (typeof window["dataUpdateCallBack"] === "function") {
    dataUpdateCallBack();
  } else {
    console.log("no function completeQueries()");
  }
}