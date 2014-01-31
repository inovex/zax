/*
 * basis
 */
function _cur_page() {
	var cur = window.location.hash
	if (!cur) cur = '#about'
	// ggf. den pfad entfernen
	cur = cur.replace(/\/.*/, '')
	return cur.substr(1)
}

// menu item auswaehlen
function update_menu() {
	$(".menu li a").removeClass('selected')
	$(".menu li a[href$=#"+_cur_page()+"]").addClass('selected')
}

function load_page() {
	// nur wenn sich die seite geaender hat
	if ($('#p_'+_cur_page()).css('display') == 'none') {
		update_menu()
		// alle anderen ausblenden
		$('#page>div').hide()
		$('#p_'+_cur_page()).slideDown()
	}
}

// startup
$(function() {
	// load page
	$(window).bind('hashchange', load_page)
	// alle anderen ausblenden
	$('#page>div').hide()
	load_page()
})


/*
 * showcase
 */
function _cur_image() {
	var cur = window.location.hash
	if (cur.indexOf('/') == -1) cur = '#showcase/tablet_problems'
	// nur den pfad
	return cur.replace(/.*\//, '')
}

// menu item auswaehlen
function update_thumb() {
	$("#col_thumbs img").removeClass('selected')
	$("#col_thumbs img[src$="+_cur_image()+"\\.png]").addClass('selected')
}

function load_image() {
	update_thumb()
	$('#col_big img').attr('src', 'img/big/'+_cur_image()+'.png')
}

// startup
$(function() {
	// load image
	$(window).bind('hashchange', load_image)
	load_image()
	$('#col_big img').click(function() {
		window.open($(this).attr('src'))
	})
})

/*
 * howto
 */
// images als thumb, onclick für gross
$(function() {
	$('#p_howto_push img').click(function() {
		window.open($(this).attr('src'))
	})
})

