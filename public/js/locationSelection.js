$(function () {
  var locationPrefArray = ['Empty'];
  var maxLocations = 4;
  var numberOfLocations = $('[data-location-name]').length;

  function getGetOrdinal(n) {
    var s=["th","st","nd","rd"], v= n%100;
    return n+(s[(v-20)%10]||s[v]||s[0]);
  }

  $('[data-location-name]').on('change', function () {
    var $this = $(this);
    var thisLocation = $this.closest('.block-label').text();
    var thisLocationId = $this.attr('id');
    var thisLocationValue = $this.attr('value');

    var arrayPosition = $.inArray(thisLocationId, locationPrefArray);
    var emptyPosition = $.inArray('Empty', locationPrefArray);

    if (arrayPosition < 0 && $this.is(':checked')) {
      if (emptyPosition < 0) {
        locationPrefArray.push(thisLocationId);
      } else {
        locationPrefArray.splice(emptyPosition, 1, thisLocationId);
      }

      var arrayPositionNow = $.inArray(thisLocationId, locationPrefArray);
      var hiddenLocationId = "#location_" + arrayPositionNow;
      $(hiddenLocationId).val(thisLocationValue);

      $('#selectedPrefList li').eq(arrayPositionNow).after(
        '<li class="med-btm-margin scheme-prefcontainer" data-location-id="' +
        thisLocationId + '"><span data-schemeorder>' +
        getGetOrdinal(arrayPositionNow + 1) +
        ' preference</span><div class="text scheme-elegrepeat"><div><span class="bold-small" data-schemenameinlist>' +
        thisLocation + '</span></div>' +
        '<a href="#" class="link-unimp location-remove"><i class="fa fa-times" aria-hidden="true"></i>Remove <span class="visuallyhidden">' + thisLocation + '</span></a>' +
        '</div>'
      );

      $this.closest('.scheme-container').addClass('selected-scheme')
        .find('.selected-preference').text(getGetOrdinal(arrayPositionNow + 1)).removeClass('invisible');

      if($('[data-location-name]:checked').length === maxLocations && $('#schemeWarning').length === 0) {
        $('[data-location-name]:not(:checked)').attr('disabled', true).closest('label').addClass('disabled');
        $('#selectedPrefList').after('<div id="schemeWarning" class="panel-info standard-panel"><p>You\'ve chosen the maximum number of locations</p></div>');
      }
    }

    if (!$this.is(':checked')) {
      var hiddenLocationId = "#location_" + arrayPosition;
      $(hiddenLocationId).val('');
      locationPrefArray.splice(arrayPosition, 1, 'Empty');
      $('#selectedPrefList').find('[data-location-id="' +
        thisLocationId + '"]').remove();
      $this.closest('.scheme-container').removeClass(
        'selected-scheme').find('.selected-preference').text(
        'N/A').addClass('invisible');

      $('[data-location-name]:not(:checked)').attr('disabled', false).closest('label').removeClass('disabled');
      $('#schemeWarning').remove();
    }

    $('#chosenLimit').addClass('hidden');
    $('#chooseYourPrefs').removeClass('hidden');

    if ($('input[data-location-name]:checked').length > 0) {
      $('[data-scheme-placeholder]').addClass(
        'toggle-content');
    } else {
      $('[data-scheme-placeholder]').removeClass(
        'toggle-content');
    }
  });

  $('#selectedPrefList').on('click', '.location-remove', function (e) {
    var thisLocation = $(this).closest('.scheme-prefcontainer').attr('data-location-id'),
      locationsAfter = $(this).closest('.scheme-prefcontainer').nextAll(),
      locationName = $(this).closest('.scheme-prefcontainer').find('[data-schemenameinlist]').text();

    e.preventDefault();

    $('#' + thisLocation).trigger('click').attr('checked', false).closest('label').removeClass('selected');

    $('#locationRemovedMessage').text(locationName + ' has been removed from your preferences');

    locationsAfter.each(function () {
      var locationId = $(this).attr('data-location-id');
      var location = $('#' + locationId);
      location.trigger('click');
      location.trigger('click');
    });
  });

  // This function reads the data in the hidden input fields and checks the corresponding
  // checkboxes in the location selection control
  function setLocationsToCheckedOnPageLoad() {
    for (var i = 0; i < numberOfLocations; i++) {
      var location = $('#location_' + i).val();
      if (location !== '') {
        var locationId = "#locations_" + location;
        var isChecked = $(locationId).is(':checked');
        if (isChecked === false) {
          $(locationId).click();
        }
      }
    }
  }

  setLocationsToCheckedOnPageLoad();
});
