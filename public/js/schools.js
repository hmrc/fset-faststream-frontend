$(function() {
  function addSchoolsDropdown(selector, idSelector) {
    $(selector).keypress(function(){
      $(idSelector).val("");
    });
    
    if ($(selector).autocomplete) {
      $(selector).autocomplete({
        source: function( request, response ) {
          var r = jsRoutes.controllers.SchoolsController.getSchools(request.term)
          r.ajax({
            success : function(data) {
              $(selector).removeClass('ui-autocomplete-loading');
  
              response( $.map( data, function(item) {
                item.label = item.label
                item.value = item.name
                return item
              }));
            },
            error: function(data) {
              $(selector).removeClass('ui-autocomplete-loading');
            }
          });
        },
        minLength: 3,
        change: function(event, ui) {},
        open: function() {},
        close: function() {},
        focus: function(event,ui) {},
        select: function(event, ui) {
          $(idSelector).val(ui.item.id);
        },
        create: function () {
          $(this).data("ui-autocomplete")._renderItem = function (ul, item) {
            if (item.value ===''){
              return $('<li class="ui-menu-item ui-state-disabled">&nbsp;&nbsp;'+item.label+'</li>').appendTo(ul);
            } else {
              return $("<li>")
                  .append("<a>" + item.label + "</a>")
                  .appendTo(ul);
            }
          };
        }
      });
    }
  }

  $('#schoolName14to16').ready(function() {
    addSchoolsDropdown("#schoolName14to16", "#schoolId14to16");
    console.log("Added dropdown to schoolName14to16")
  });
  $('#schoolName16to18').ready(function() {
    addSchoolsDropdown("#schoolName16to18", "#schoolId16to18");
    console.log("Added dropdown to schoolName16to18")
  });
});
