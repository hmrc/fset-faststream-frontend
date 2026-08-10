/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(function() {
    $(':checkbox').change(function(){
        var preferNotToSay = 'Prefer not to say'.toLowerCase();
        var thisDisabilityValue = $(this).attr('value');
        // If we add more disability categories, this will change and the number will need to be updated
        var preferNotToSayCheckboxItemId = '#disabilityCategories-11';

        // If the user clicks the preferNotToSay checkbox then uncheck all the other options
        if (thisDisabilityValue.toLowerCase() === preferNotToSay) {
            if (this.checked) {
                $("input:checkbox").not(preferNotToSayCheckboxItemId).prop("checked", false);
            }
        } else {
            // Otherwise if any other checkbox is clicked, uncheck the preferNotToSay checkbox
            if (this.checked) {
                $(preferNotToSayCheckboxItemId).prop("checked", false);
            }
        }
    });
});
