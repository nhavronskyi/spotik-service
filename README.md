# spotik-service
<h3>Overview: Spotik</h3>
<p>
Team project where we wanted to create an API and telegram bot that could show you
information about your Spotify account and filter your account from artists by
chosen countries.
</p>
<h2>API</h2>
<table>
<thead>
  <tr>
    <th colspan="4">http://localhost:8080/</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td>METHOD</td>
    <td>REQUEST</td>
    <td>DESCRIPTIONS</td>
    <td>ARGUMENTS</td>
  </tr>
  <tr>
    <td rowspan="6">GET</td>
    <td>playlists</td>
    <td>shows all the playlists in your account</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>albums</td>
    <td>shows all the albums in your account</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>songs</td>
    <td>shows all the saved songs in your account</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>last-releases</td>
    <td>shows all the latest releases from the artists you've subscribed to</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>show</td>
    <td>shows all songs in the playlist by country</td>
    <td>YES</td>
  </tr>
  <tr>
    <td>account-scan</td>
    <td>gets all songs from the account by country</td>
    <td>YES</td>
  </tr>
  <tr>
    <td rowspan="2">DELETE</td>
    <td>remove-all-tracks-from-playlist</td>
    <td>removes all tracks from the playlist by country</td>
    <td>YES</td>
  </tr>
  <tr>
    <td>remove-track-from-playlist</td>
    <td>remove track from playlist</td>
    <td>YES</td>
  </tr>
</tbody>
</table>
<h2>Quick Start</h2>
To get the project off to a good start, you need to start each service in the right direction:
<ol>
<li> <strong>spotik-config-service</strong> -> https://github.com/nhavronskyi/spotik-config-service</li>
<li> <strong>spotik-service</strong> -> https://github.com/nhavronskyi/spotik-service</li>
<li> <strong>spotik-telegram-service</strong>strong> -> https://github.com/nhavronskyi/spotik-telegram-service</li>
</ol>


