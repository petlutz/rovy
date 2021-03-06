package de.gnox.rovy.web;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.gnox.rovy.api.RovyCom;
import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyCommandType;

/**
 * Servlet implementation class RoverComServlet
 */
@WebServlet("/RovyComServlet")
public class RovyComServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RovyComServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			RovyCom rovyCom = null;
			try {
				rovyCom = (RovyCom) Naming.lookup("rmi://127.0.0.1:1234/RovyCom");
			} catch (NotBoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			
			String page = request.getParameter("page");
		
			if (page == null)
				page = "com";
			
			if (page.equals("media")) 
				showMediaPage(request, response);
			else if (page.equals("com")) 
				showComPage(request, response, rovyCom);
			else
				response.getWriter().append("unknown page");
			
		} catch (Exception e) {
			// logout
			// request.getSession().setAttribute("sessionId", null);
			e.printStackTrace();
			response.getWriter().append("Exception occured: " + e.getMessage());
			throw e;
		}
	}

	// private void showTelemetryPage(HttpServletRequest request,
	// HttpServletResponse response, Rover rover)
	// throws IOException {
	// writeTelemetryPage(response.getWriter(), rover);
	// }

	// private void showLoginPage(HttpServletRequest request,
	// HttpServletResponse response) throws IOException {
	// PrintWriter w = response.getWriter();
	// w.append("<html><body><center>");
	// w.append("<img src='icon2_64.png'>");
	// w.append("<form action='RovyComServlet'>");
	// w.append("<input type='text' name='pwd'><br>");
	// w.append("<button type='submit'>login</button><br>");
	// w.append("</form>");
	// w.append("</center></body></html>");
	// }
	
	private void showMediaPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		
		
		PrintWriter w = response.getWriter();
		
		
		w.append("<html><body>");
		w.append("<a href='RovyComServlet'><img src='icon2_64.png'></a><br><br>");

		List<String> files = new ArrayList<>(getServletContext().getResourcePaths("/"));
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		
		for (String file : files)
			if (file.startsWith("/rovy"))
				w.append("<a href='" + getServletContext().getContextPath() + file + "'>" + file + "</a><br>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='ClearMediaCache'>clear</button>");
		w.append("</form>");
		w.append("</body></html>");
		
	}

	private void showComPage(HttpServletRequest request, HttpServletResponse response, RovyCom rover)
			throws IOException {
		String commandType = request.getParameter("command");
		if (commandType != null) {
			RovyCommand command = new RovyCommand(RovyCommandType.valueOf(commandType),
					new HashMap<>(request.getParameterMap()));
			rover.performCommand(command);
		}
		writeComPage(response.getWriter(), rover);
	}

	private void writeComPage(PrintWriter w, RovyCom rover) throws RemoteException {
		w.append("<html><body>");

		w.append("<table><tr><td valign='top' style='width:300; min-width:300; background-color: #abc; padding:8;'>");
		w.append(
				"<center><p><a href='RovyComServlet'><img src='icon2_64.png'></a></p><p><strong>. . R o v y C o m . .</strong></p></center>"
						+ "");
		w.append("<hr>");
		w.append("<form action='RovyComServlet'>");
		w.append("<input type='text' size='5' name='degrees'>deg<br>");
		w.append("<button type='submit' name='command' value='Turn'>&#x21B6;&#x21B7;</button>");
		w.append("<button type='submit' name='command' value='TurnFwd'>&#x21B6;&#x2191;&#x21B7;</button>");
		w.append("<button type='submit' name='command' value='TurnBkw'>&#x21B6;&#x2193;&#x21B7;</button>");
		w.append("</form>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<input type='text' size='5' name='degrees'>deg<br>");
		w.append("<button type='submit' name='command' value='Slide'>&#x2194;</button>");
		w.append("</form>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<input type='hidden' name='command' value='Turn'>");
		w.append("<button type='submit' name='degrees' value='-10'>&#x21B6;10</button>");
		w.append("<button type='submit' name='degrees' value='-30'>&#x21B6;30</button>");
		w.append("<button type='submit' name='degrees' value='-90'>&#x21B6;90</button>");
		w.append("<button type='submit' name='degrees' value='-180'>&#x21B6;180</button><br>");
		w.append("<button type='submit' name='degrees' value='10'>&#x21B7;10</button>");
		w.append("<button type='submit' name='degrees' value='30'>&#x21B7;30</button>");
		w.append("<button type='submit' name='degrees' value='90'>&#x21B7;90</button>");
		w.append("<button type='submit' name='degrees' value='180'>&#x21B7;180</button>");
		w.append("</form>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<input type='text' size='5' name='cm'>cm<br>");
		w.append("<button type='submit' name='command' value='Drive'><b>&#x2191;&#x2193;</b></button>");
		w.append("</form>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<input type='hidden' name='command' value='Drive'>");
		w.append("<button type='submit' name='cm' value='5'>&#x2191;5</button>");
		w.append("<button type='submit' name='cm' value='10'>&#x2191;10</button>");
		w.append("<button type='submit' name='cm' value='50'>&#x2191;50</button>");
		w.append("<button type='submit' name='cm' value='100'>&#x2191;100</button><br>");
		w.append("<button type='submit' name='cm' value='-5'>&#x2193;5</button>");
		w.append("<button type='submit' name='cm' value='-10'>&#x2193;10</button>");
		w.append("<button type='submit' name='cm' value='-50'>&#x2193;50</button>");
		w.append("<button type='submit' name='cm' value='-100'>&#x2193;100</button>");
		w.append("</form>");
		
		w.append("<hr>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='CapturePicture'>capture picture</button>");
		w.append("<button type='submit' name='command' value='CaptureBigPicture'>capture big picture</button>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("<input size='5' type='text' name='seconds'>sec<br>");
		w.append("<button type='submit' name='command' value='CaptureVideo'>capture video</button>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='CamUp'>cam up</button>");
		w.append("<button type='submit' name='command' value='CamDown'>cam down</button>");
		w.append("<button type='submit' name='command' value='CamForeward'>look foreward</button>");
		w.append("<button type='submit' name='command' value='CamToPowerbank'>look to powerbank</button>");
		w.append("</form>");
		w.append("</form>");
		w.append("<hr>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='LightOn'>light on</button>");
		w.append("<button type='submit' name='command' value='LightOff'>light off</button>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("<select name='mode'>");
		w.append("<option>Video</option>");
		w.append("<option>Picture</option>");
		w.append("<option>PictureWithVideo</option>");
		w.append("</select>");
		w.append("<button type='submit' name='command' value='SetCapturingMode'>set capturingmode</button>");
		w.append("</form>");
		w.append("<hr>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='Dance'>dance</button>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='MakeNoise'>noise</button>");
		w.append("</form>");
		w.append("<hr>");
		w.append("<a href='RovyComServlet?page=media'>media</a>");

		w.append("</td><td valign='top'>");
		String camPicture = rover.getCamPicture();
		String camVideo = rover.getCamVideo();
		if (camVideo != null) {
			w.append("<p><video width='640' height='480' controls autoplay>");
			w.append(" <source src='" + camVideo + "' type='video/mp4'>");
			w.append("Your browser does not support the video tag.");
			w.append("</video></p>");
		}
		if (camPicture != null)
			w.append("<p><img src='" + camPicture + "' alt='no image'></p>");

		w.append("<p><u>Telemetry Data</u>");
		w.append("<pre>");
		rover.getTelemetryData().getEntries().forEach(msg -> {
			w.append(msg + "<br>");
		});
		w.append("</pre></p>");

		w.append("</td></tr></table>");
		// w.append("Telemetry Data: " + telemetryData);
		w.append("</body></html>");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
