/*
 * Copyright 2016 HM Revenue & Customs
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

    const departments = [
        {"label": "Accountant in Bankruptcy"},
        {"label": "Advisory, Conciliation and Arbitration Service"},
        {"label": "Animal and Plant Health Agency"},
        {"label": "Attorney General's Office"},
        {"label": "Building Digital UK"},
        {"label": "Cabinet Office"},
        {"label": "Centre for Environment, Fisheries and Aquaculture Science"},
        {"label": "Charity Commission"},
        {"label": "Companies House"},
        {"label": "Competition and Markets Authority"},
        {"label": "Criminal Injuries Compensation Authority"},
        {"label": "Crown Commercial Service"},
        {"label": "Crown Office and Procurator Fiscal Service"},
        {"label": "Crown Prosecution Service"},
        {"label": "Defence Electronics and Components Agency"},
        {"label": "Defence Equipment and Support"},
        {"label": "Defence Science and Technology Laboratory"},
        {"label": "Department for Business and Trade"},
        {"label": "Department for Culture, Media and Sport"},
        {"label": "Department for Education"},
        {"label": "Department for Energy Security and Net Zero"},
        {"label": "Department for Environment, Food and Rural Affairs"},
        {"label": "Department for Levelling Up, Housing and Communities"},
        {"label": "Department for Science, Innovation and Technology"},
        {"label": "Department for Transport"},
        {"label": "Department for Work and Pensions"},
        {"label": "Department of Health and Social Care"},
        {"label": "Disclosure Scotland"},
        {"label": "Driver and Vehicle Licensing Agency"},
        {"label": "Driver and Vehicle Standards Agency"},
        {"label": "Education and Skills Funding Agency"},
        {"label": "Education Scotland"},
        {"label": "Estyn"},
        {"label": "FCDO Services"},
        {"label": "Food Standards Agency"},
        {"label": "Food Standards Scotland"},
        {"label": "Foreign, Commonwealth and Development Office"},
        {"label": "Forestry and Land Scotland"},
        {"label": "Government Actuary's Department"},
        {"label": "Government Internal Audit Agency"},
        {"label": "Government Legal Department"},
        {"label": "Government Property Agency"},
        {"label": "Health and Safety Executive"},
        {"label": "HM Courts and Tribunals Service"},
        {"label": "HM Crown Prosecution Service Inspectorate"},
        {"label": "HM Land Registry"},
        {"label": "HM Prison and Probation Service"},
        {"label": "HM Revenue and Customs"},
        {"label": "HM Treasury"},
        {"label": "Home Office"},
        {"label": "Institute for Apprenticeships and Technical Education"},
        {"label": "Intellectual Property Office"},
        {"label": "Legal Aid Agency"},
        {"label": "Maritime and Coastguard Agency"},
        {"label": "Medicines and Healthcare Products Regulatory Agency"},
        {"label": "Met Office"},
        {"label": "Ministry of Defence"},
        {"label": "Ministry of Justice"},
        {"label": "National Crime Agency"},
        {"label": "National Infrastructure Commission"},
        {"label": "National Records of Scotland"},
        {"label": "National Savings and Investments"},
        {"label": "Northern Ireland Office"},
        {"label": "Office for Budget Responsibility"},
        {"label": "Office for Standards in Education, Children's Services and Skills"},
        {"label": "Office of Gas and Electricity Markets"},
        {"label": "Office of Qualifications and Examinations Regulation"},
        {"label": "Office of Rail and Road"},
        {"label": "Office of the Public Guardian"},
        {"label": "Office of the Scottish Charity Regulator"},
        {"label": "Office of the Secretary of State for Scotland"},
        {"label": "Office of the Secretary of State for Wales"},
        {"label": "Planning Inspectorate"},
        {"label": "Queen Elizabeth II Centre"},
        {"label": "Registers of Scotland"},
        {"label": "Revenue Scotland"},
        {"label": "Royal Fleet Auxiliary"},
        {"label": "Rural Payments Agency"},
        {"label": "Scottish Courts and Tribunals Service"},
        {"label": "Scottish Forestry"},
        {"label": "Scottish Government"},
        {"label": "Scottish Housing Regulator"},
        {"label": "Scottish Prison Service"},
        {"label": "Scottish Public Pensions Agency"},
        {"label": "Serious Fraud Office"},
        {"label": "Social Security Scotland"},
        {"label": "Student Awards Agency for Scotland"},
        {"label": "Submarine Delivery Agency"},
        {"label": "Teaching Regulation Agency"},
        {"label": "The Insolvency Service"},
        {"label": "The National Archives"},
        {"label": "Transport Scotland"},
        {"label": "UK Debt Management Office"},
        {"label": "UK Export Finance"},
        {"label": "UK Health Security Agency"},
        {"label": "UK Hydrographic Office"},
        {"label": "UK Space Agency"},
        {"label": "UK Statistics Authority"},
        {"label": "UK Supreme Court"},
        {"label": "Valuation Office Agency"},
        {"label": "Vehicle Certification Agency"},
        {"label": "Veterinary Medicines Directorate"},
        {"label": "Water Services Regulation Authority"},
        {"label": "Welsh Government"},
        {"label": "Welsh Revenue Authority"},
        {"label": "Wilton Park"}
    ];

    function setDepartmentData(label) {
        $('#civilServiceExperienceDetails_civilServantDepartmentSelected').val(label);
        $('#civilServiceExperienceDetails_civilServantDepartment').val(label);
    }

    $("#civilServiceExperienceDetails_civilServantDepartmentSelected").autocomplete({
        source: departments,
        // Triggered when an item is selected from the menu.
        select: function (e, ui) {
            setDepartmentData(ui.item.label);
            console.log("select - data set to " + ui.item.label);
        },
        // Triggered when the field is blurred, if the value has changed.
        change: function (event, ui) {
            if (ui.item != null) {
                console.log("change - ui.item.label = " + ui.item.label);
            } else {
                console.log("change - ui.item = " + ui.item);
            }
            if (ui.item == null) {
                setDepartmentData('', '');
                console.log("change - data cleared");
            }
        }
    });

    const value = $("#civilServiceExperienceDetails_civilServantDepartment").val();
    console.log("value in hidden field = " + value);

    if (value != null && value !== "") {
        const obj = departments.find(o => o.label === value);
        $('#civilServiceExperienceDetails_civilServantDepartmentSelected').val(obj.label);
        console.log("set the selected value to " + obj.label);
    }

    // Hide the error message associated with the hidden field
    $("#civilServiceExperienceDetails_civilServantDepartment_field").css("visibility", "hidden");
});