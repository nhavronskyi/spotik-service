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
    <th colspan="3">http://localhost:8080/</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td>METHOD</td>
    <td>REQUEST</td>
    <td>ARGUMENTS</td>
  </tr>
  <tr>
    <td rowspan="6">GET</td>
    <td>playlists</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>albums</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>songs</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>last-releases</td>
    <td>NO</td>
  </tr>
  <tr>
    <td>show</td>
    <td>YES</td>
  </tr>
  <tr>
    <td>account-scan</td>
    <td>YES</td>
  </tr>
  <tr>
    <td rowspan="2">DELETE</td>
    <td>remove-all-tracks-from-playlist</td>
    <td>YES</td>
  </tr>
  <tr>
    <td>remove-track-from-playlist</td>
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


