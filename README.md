# YgosinglesScraper
This program checks for the availability of yugioh cards at ygosingles website :)

VERSION SHIT.0
Usage java Scraper <ygosingles_page> <recipient> <sender@gmail.com> <sender password> <card_id1> <card_id2> ... <card_idn>

Example for ygosingles_page: https://ygosingles.com/collections/maximum-crisis/secret-rare
Example for card_id: MACR036

Dependencies: jsoup, javax.mail

Uses gmail smtp since most ISPs blocks email port.
Will probably update to oAuth at some point.
Dream is to have a GUI.
Just create a burner gmail for sender if you're afraid xd
If the sender email uses 2 factor authentication, create an "App password" instead.
