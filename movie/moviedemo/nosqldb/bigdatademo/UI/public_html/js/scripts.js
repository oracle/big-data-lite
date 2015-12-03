jQuery(document).ready(
  function(){ 
    for (var i = 0; i < cats.length ; i++){createCarousel (cats[i]);}
    createBubble(0,0,"");
    //TODO SEARCH
    //$("#rating").msDropDown();
    //$("#genre").msDropDown();
    $('#loading').hide();
    $("#getMoviesMood").click(
      function(){
        $.get("moviesByMood.jsp", function(resp){
          var tmp = resp;
          tmp = tmp.replace(/^\s+|\s+$/g, '') ;
          if (tmp == "error") $("#getMoviesMood").attr('disabled','disabled');
          else $("#slideboxMood").empty().append(resp);
        });
    ;});
});

function onPlayerReady(event) {}

function stopVideo() {
  try{
    if (player){
      player.pauseVideo();
    }
  }catch(err){
    
  }
}

function loadYTVideo(videoId,position) {
  $("#player").replaceWith('<div id="player"></div>');
  player = new YT.Player('player', { 
    videoId: videoId,
    height: '390',
      width: '640',
      playerVars: { 'autoplay': 1, 'start' : position},
      events: {
        'onReady': onPlayerReady,
        'onStateChange': onPlayerStateChange
      }
  });
}


function onPlayerStateChange(event) {
  var movieId = $("#movie").attr('movieid');
  if (event.data == YT.PlayerState.PAUSED){
      $.get("movieActivity.jsp" ,{ movieId : movieId , time : player.getCurrentTime() , type : "pause" }, function(resp){});
  }
  if (event.data == YT.PlayerState.PLAYING){
      if (Math.floor(player.getCurrentTime()) < 2)
        $.get("movieActivity.jsp" ,{ movieId : movieId , time : "0" , type : "purchase" }, function(resp){});
      $.get("movieActivity.jsp" ,{ movieId : movieId , time : "" , type : "play" }, function(resp){});
  }
  if (event.data == YT.PlayerState.ENDED){
      $.get("movieActivity.jsp" ,{ movieId : movieId , type : "finish" }, function(resp){});
      $('#movie').fadeOut(300 , function() {$('#mask').remove();});
      $('#movie-information').fadeOut(300 , function() {$('#mask-information').remove();});
      $.get("refreshFirstPart.jsp", function(resp){$("#firstPart").empty().append(resp);});
      $.get("refreshLastpart.jsp", function(resp){$("#lastPart").empty().append(resp);});
  }
}

function continueWatching(){
  $("#mycarouselCW .item-box").each(function(){
      $(this).unbind('click');
      $(this).click(function(e){
        e.preventDefault();
        showPopUp("mask","#movie","#closeMovie",$(this).attr('movieid'));
        $.get("getPlayer.jsp" ,{id : $(this).attr('movieid')}, function(resp){$("#player-script").empty().append(resp);});
      });
  });
}

$(document).ajaxStart(function(){
    $('#loading').show(); 
    $(document).bind('mousemove', function(e){ $('#loading').css({left: e.pageX, top: e.pageY}); });
}).ajaxStop(function(){ 
    $('#loading').hide();
    $(document).unbind('mousemove');
});

function createBubble(castId,typeCast,containerId){
    $(containerId+" .item-box").each(function(){
       $(this).click(function(e){
          e.preventDefault();
          if (!$(e.target).is("a")){
            $.get($(this).attr('rel')+"&show=1", function(resp){
              $("#movie-content").empty().append(resp);
              setMovieEvents(typeCast);
            });
          }
       });
    });
}

function setRating(rated,className,movieId){
    $("input."+className).rating({callback: function(value, link){
        $("div."+className).each(function(){
        if ($(this).hasClass('star-rating-on')) $(this).addClass('star-rating-changed'); });
        $.get("movieActivity.jsp" ,{ movieId : movieId , type : "rate", rate : value }, function(resp){});
        ratedStars();
    }});
    if (rated) ratedStars();
    function ratedStars(){
      $("div."+className).each(function(){
          if ($(this).hasClass('star-rating-on')) {$(this).addClass('star-rating-changed');}
          $(this).hover(function(){$(this).parent().find('.star-rating-changed').removeClass('star-rating-changed');
          },function(){$(this).parent().find('.star-rating-on').addClass('star-rating-changed');})
        });
    }
}

function setMovieEvents(castId,typeCast){
    showPopUp("mask-information","#movie-information", "#closeInformation","");
    popUp();
    $("#movie-content .information .rightInfoPanel a").each(function(){
        $(this).click(function(e){
            e.preventDefault();
            var type = $(this).attr('type');
            var id = $(this).attr('href');
            $.get("getMoviesByCast.jsp" ,{ id : id , type : type }, function(r){
                $("#moviesByCast").empty().append(r);
                createCarousel("Cast");
                getMovieDescription();
            });
        });
    });
}

function getMovieDescription(castId,typeCast){
  $("#moviesByCast .item-box").each(function(){
      $(this).click(function(e){
          e.preventDefault();
          $.get($(this).attr('rel')+"&show=0", function(resp){
              $(".information").empty().append(resp);
              setMovieEvents(castId,typeCast);
          });
      });
  });
}

function showPopUp(maskId,box,close,movieId){
  $(box).fadeIn(0);
  var popMargTop = ($(box).height() + 24) / 2; 
  var popMargLeft = ($(box).width() + 24) / 2; 
  $(box).css({'margin-top' : -popMargTop,'margin-left' : -popMargLeft});
  $('body').append('<div id="'+maskId+'"></div>');
  if (box == "#movie") $(box).attr('movieid',movieId);
  $('#'+maskId).fadeIn(0);
  $(close).live('click', function(e) {
      stopVideo();
      e.preventDefault();
      $('#'+maskId+', '+box).hide(300 , function() {}); 
      $.get("refreshFirstPart.jsp", function(resp){$("#firstPart").empty().append(resp);});
      $.get("refreshLastpart.jsp", function(resp){$("#lastPart").empty().append(resp);});
      $('#'+maskId).remove();
  });
}

function createCarousel (i) {
    jQuery('#mycarousel'+i).jcarousel({
        initCallback: function(carousel){
          jQuery('#nextSlide'+i).bind('click', function() {carousel.next();return false;});
          jQuery('#prevSlide'+i).bind('click', function() {carousel.prev();return false;});
        },
        scroll: 1,
        wrap: 'circular',
        buttonNextHTML: null,
        buttonPrevHTML: null,
        itemFallbackDimension: 150
    });
}

/* BUBBLE */
function popUp(){
  $('.playButton').click(function(e) {
      e.preventDefault();
      showPopUp("mask",$(this).attr('href'),"#closeMovie", $(this).attr('movieid'));
      $.get("getPlayer.jsp" ,{id : $(this).attr('movieid')}, function(resp){
      $("#player-script").empty().append(resp);});
    });
}

