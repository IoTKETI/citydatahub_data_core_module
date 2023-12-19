function showModal(){
  var $wrap = $('.wrap');
  var $buttonModalShow = '.button__modal--show';
  var $this, $id;

  $wrap.on('click', $buttonModalShow, function(e){
    $this = $(this);
    $id = $this.attr('href');
    openModal($id, $this);
  });

  function openModal(selector, $opener) {
    var $modal = $(selector);
    var $btnClose = $modal.find('.button__modal--close');

    $modal.show().attr("tabindex", 0).focus();
    $btnClose.on("click.layerClose", function(){
      closeModal(selector, $opener);
    })
    $modal.on("keydown.esc", function(e){
        if (e.which === 27) {
            $btnClose.trigger("click");
        }
    });
  }

  function closeModal(selector, $opener) {
    var $modal = $(selector);
    var $btnClose = $modal.find('.button__modal--close');

    $btnClose.off("click.layerClose");
    $modal.hide().removeAttr("tabindex").off("keydown.esc");
    $opener.focus();
  }
}


function showMenu(){
  var navDepth1Active = 'nav__depth1--active',
      navDepth1       = '.nav__depth1',
      navDepth2       = '.nav__depth2',
      speed           = 300,
      $body           = $('body'),
      $navDepth1      = $('.nav__depth1'),
      $navDepth2      = $('.nav__depth2'),
      $this;

  $navDepth1.on('click', function(e){
    $this = $(this);
    $this.find(navDepth2).slideToggle(speed);
    $this.siblings().removeClass(navDepth1Active).find(navDepth2).slideUp(speed);
    if($this.hasClass(navDepth1Active)) return $this.removeClass(navDepth1Active);
    if($this.find('> .nav__link').hasClass('nav__button')){
      $navDepth1.removeClass(navDepth1Active);
      $this.addClass(navDepth1Active);
    }
  })

  $body.on('mouseenter', '.wrap--wide .nav__depth1', function(){
    $(this).removeClass(navDepth1Active);
  });
  $body.on('mouseleave', '.wrap--wide .nav__depth1', function(){
    $(this).removeClass(navDepth1Active)
    $navDepth2.hide();
  });
}

function showNav(){
  var $btnNavToggle = $('.button__nav--toggle'),
      $navDepth1    = $('.nav__depth1'),
      $navDepth2    = $('.nav__depth2'),
      $wrap         = $('.wrap'),
      wrapFull      = 'wrap--wide';

  $btnNavToggle.on('click', function(){
    $wrap.hasClass(wrapFull) ? closeNav() : openNav();
  });

  function closeNav(){
    $wrap.removeClass(wrapFull);
    $btnNavToggle.attr('title','메뉴 접기');
    $navDepth1.off('mouseenter').off('mouseleave');
  };

  function openNav(){
    $wrap.addClass(wrapFull);
    $btnNavToggle.attr('title','메뉴 펼치기');
    $navDepth1.removeClass('nav__depth1--active');
    $navDepth2.hide();
  };
}


function uploadFile(){
  var $wrap = $('.wrap');
  var $fileList = $('.file__list');
  var $fileItem = $('.file__list > li');
  var $this, $fileName;

  $('.file__group [type="file"]').on('change', function(){
    $this = $(this);
    $fileName = $this.val().split('/').pop().split('\\').pop()
    if(!($this.val())) return;
    addFile($fileName)
  });

  $wrap.on('click', '.button__file-delete', function(e){
    deleteFile(e)
  });

  function addFile(newName){
    var $newFileItem = '<li class="file__item">' + newName + '<button type="button" class="button__file-delete material-icons" title="파일 삭제"><span class="hidden">파일 삭제</span></button></li>';
    if($fileItem.length <= 0) return;
    $('.file__item--none').remove();
    $fileList.append($newFileItem);
  }

  function deleteFile(e){
    if ($fileItem.length <= 0) return;
    $(e.target).parents('li').remove();
    initFile();
  }

  function initFile(){
    $noFileItem  = '<li class="file__item file__item--none">선택된 파일이 없습니다.</li>';
    if($('.file__item').length >= 1) return;
    $fileList.append($noFileItem);
  }
}


// $.datepicker.setDefaults({
//   dateFormat: 'yy-mm-dd',
//   showOtherMonths: true,
//   showMonthAfterYear:true,
//   changeYear: true,
//   changeMonth: true,
//   buttonText: "선택",
//   yearSuffix: "년",
//   monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
//   monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
//   dayNamesMin: ['일','월','화','수','목','금','토'],
//   dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
// });


function setInputNumber() {
  var $input = $('.input__number'),
      $buttonUp = $('.button__number--up'),
      $buttonDown = $('.button__number--down'),
      $minValue = $input.attr('min'),
      // $maxValue = $input.attr('max'),
      $value, $newValue;


  $buttonUp.click(function() {
    $value = parseFloat($input.val());
    // if ($value >= $maxValue) return $newValue = $value;
    $newValue = $value + 1;
    $input.val($newValue).trigger('change');
  });

  $buttonDown.click(function() {
    $value = parseFloat($input.val());
    if ($value <= $minValue) return;
    $newValue = $value - 1;
    $input.val($newValue).trigger('change');
  });
}
