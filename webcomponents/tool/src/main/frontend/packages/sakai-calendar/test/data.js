export const i18nUrl = /.*getI18nProperties.*calendar-wc/;

export const i18n = `
widget_title=Calendar
today=Today
days_message=Showing events for the next {} days
events_for=Events for
`;

export const userId = "adrian";
export const siteId = "xyz";
export const siteTitle = "XYZ Site";

export const vavavoom = "Vavavoom";
export const vavavoomSite = "Vavavoom Site";

export const selectedDate = new Date(1677269640000);

export const userCalendarUrl= "/api/users/current/calendar";

export const userCalendarEvents = {
  events: [
    { tool: "Assignments", title: "Assignment One Due", url: "/assn/1", start: 1677269640500, siteTitle },
  ]
};

export const siteCalendarEvents = [
  { subject: "Ears", url: "/annc/ears", siteTitle, siteId },
  { subject: "Chips", url: "/annc/chips", siteTitle, siteId },
];
