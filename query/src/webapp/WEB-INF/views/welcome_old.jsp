<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Qwiki Search</title>
	
	<spring:url value="/resources/css/bootstrap.min.css" var="bootstrapCss" />
	<link href="${bootstrapCss}" rel="stylesheet" />
	<spring:url value="/resources/js/jquery-3.1.1.min.js" var="jqueryJs" />
	<script src="${jqueryJs}"></script>
	<spring:url value="/resources/js/search.js" var="searchJs" />
	<script src="${searchJs}"></script>
	<spring:url value="/resources/css/landing-page.css" var="customCss" />
	<link href="${customCss}" rel="stylesheet" />
	<spring:url value="/resources/css/font-awesome.min.css" var="fontCss" />
	<link href="${fontCss}" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Lato:300,400,700,300italic,400italic,700italic" rel="stylesheet" type="text/css">
</head>

<body>
    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-fixed-top topnav" role="navigation">
        <div class="container topnav">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand topnav" href="#">Qwiki Search</a>
            </div>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <a href="#about">About</a>
                    </li>
                    <li>
                        <a href="#services">Articles</a>
                    </li>
                    <li>
                        <a href="#contact">Login</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container -->
    </nav>


    <!-- Header -->
    <a name="about"></a>
    <div class="intro-header">
        <div class="container">
            <div class="row">
                <div class="col-lg-12">
                    <div class="intro-message">
                        <h1>Qwiki Search</h1>
                        <h3>A Simple Wikipedia Search Engine</h3>
						<div class="col-md-2"></div>
						<div class="col-md-8">
							<div class="well-searchbox">
								<form class="form-horizontal" role="form">
									<div class="row">
										<div class="form-group">
											<div class="col-md-12">
												<input type="text" class="form-control" placeholder="Enter Query">
											</div>
										</div>
									</div>
									
									<!--
									<div class="form-group">
										<label class="col-md-4 control-label">Category</label>
										<div class="col-md-8">
											<select class="form-control" placeholder="Category">
												<option value="">All</option>
												<option value="">Category 1</option>
												<option value="">Category 2</option>
											</select>
										</div>
									</div>
									-->
									<div class="row">
										<div class="col-sm-offset-3 col-sm-5">
											<button type="submit" class="btn btn-success">Search</button>
										</div>
									</div>
								</form>
							</div>
						</div>
						<div class="col-md-2"></div>
                    </div>
                </div>
            </div>

        </div>
        <!-- /.container -->

    </div>
    <!-- /.intro-header -->

    <!-- Page Content -->

	<div id="results">
		<a  name="services"></a>
		<div class="content-section-a">

			<div class="container">
				<div class="row">
					<div class="col-lg-5 col-sm-6">
						<hr class="section-heading-spacer">
						<div class="clearfix"></div>
						<h2 class="section-heading">Death to the Stock Photo:<br>Special Thanks</h2>
						<p class="lead">A special thanks to <a target="_blank" href="http://join.deathtothestockphoto.com/">Death to the Stock Photo</a> for providing the photographs that you see in this template. Visit their website to become a member.</p>
					</div>
					<div class="col-lg-5 col-lg-offset-2 col-sm-6">
						<img class="img-responsive" src="img/ipad.png" alt="">
					</div>
				</div>

			</div>
			<!-- /.container -->

		</div>
		<!-- /.content-section-a -->

		<div class="content-section-b">

			<div class="container">

				<div class="row">
					<div class="col-lg-5 col-lg-offset-1 col-sm-push-6  col-sm-6">
						<hr class="section-heading-spacer">
						<div class="clearfix"></div>
						<h2 class="section-heading">3D Device Mockups<br>by PSDCovers</h2>
						<p class="lead">Turn your 2D designs into high quality, 3D product shots in seconds using free Photoshop actions by <a target="_blank" href="http://www.psdcovers.com/">PSDCovers</a>! Visit their website to download some of their awesome, free photoshop actions!</p>
					</div>
					<div class="col-lg-5 col-sm-pull-6  col-sm-6">
						<img class="img-responsive" src="img/dog.png" alt="">
					</div>
				</div>

			</div>
			<!-- /.container -->

		</div>
		<!-- /.content-section-b -->

		<div class="content-section-a">

			<div class="container">

				<div class="row">
					<div class="col-lg-5 col-sm-6">
						<hr class="section-heading-spacer">
						<div class="clearfix"></div>
						<h2 class="section-heading">Google Web Fonts and<br>Font Awesome Icons</h2>
						<p class="lead">This template features the 'Lato' font, part of the <a target="_blank" href="http://www.google.com/fonts">Google Web Font library</a>, as well as <a target="_blank" href="http://fontawesome.io">icons from Font Awesome</a>.</p>
					</div>
					<div class="col-lg-5 col-lg-offset-2 col-sm-6">
						<img class="img-responsive" src="img/phones.png" alt="">
					</div>
				</div>

			</div>
			<!-- /.container -->

		</div>
		<!-- /.content-section-a -->
	
	</div>
	
	<a  name="contact"></a>
    <div class="banner">

        <div class="container">

            <div class="row">
                <div class="col-lg-6">
                    <h2>Connect to Qwiki Search:</h2>
                </div>
                <div class="col-lg-6">
                    <ul class="list-inline banner-social-buttons">
                        <li>
                            <a href="https://twitter.com/SBootstrap" class="btn btn-default btn-lg"><i class="fa fa-twitter fa-fw"></i> <span class="network-name">Twitter</span></a>
                        </li>
                        <li>
                            <a href="https://github.com/IronSummitMedia/startbootstrap" class="btn btn-default btn-lg"><i class="fa fa-github fa-fw"></i> <span class="network-name">Github</span></a>
                        </li>
                        <li>
                            <a href="#" class="btn btn-default btn-lg"><i class="fa fa-linkedin fa-fw"></i> <span class="network-name">Linkedin</span></a>
                        </li>
                    </ul>
                </div>
            </div>

        </div>
        <!-- /.container -->

    </div>
    <!-- /.banner -->

    <!-- Footer -->
    <footer>
        <div class="container">
            <div class="row">
                <div class="col-lg-12">
                    <ul class="list-inline">
                        <li>
                            <a href="#">Search</a>
                        </li>
                        <li class="footer-menu-divider">&sdot;</li>
                        <li>
                            <a href="#about">About</a>
                        </li>
                        <li class="footer-menu-divider">&sdot;</li>
                        <li>
                            <a href="#services">Articles</a>
                        </li>
                        <li class="footer-menu-divider">&sdot;</li>
                        <li>
                            <a href="#contact">Login</a>
                        </li>
                    </ul>
                    <p class="copyright text-muted small">Copyright &copy; Qwiki Inc. LLC 2016. All Rights Reserved</p>
                </div>
            </div>
        </div>
    </footer>

</body>

</html>
