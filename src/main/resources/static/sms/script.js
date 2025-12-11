$(document).ready(function () {
  function getSMS() {
    return $("#smsInput").val().trim();
  }

  function getGuess() {
    return $("input[name='guess']:checked").val().trim();
  }

  function cleanResult() {
    $("#result").removeClass("correct");
    $("#result").removeClass("incorrect");
    $("#result").removeClass("error");
    $("#result").html();
  }

  $("#checkButton").click(function (e) {
    e.stopPropagation();
    e.preventDefault();

    var sms = getSMS();
    var guess = getGuess();

    startLoading();

    $.ajax({
      type: "POST",
      url: "./",
      data: JSON.stringify({ sms: sms, guess: guess }),
      contentType: "application/json",
      dataType: "json",
      success: function (res) {
        stopLoading();
        handleResult(res);
      },
      error: function (err) {
        stopLoading();
        handleError(err);
      },
    });
  });

  function handleResult(res) {
    var wasRight = res.result == getGuess();
    cleanResult();

    if (wasRight) {
      $("#result").html(`
            <div class="flex items-center p-4 mb-4 text-green-800 border border-green-200 rounded-lg bg-green-50 gap-2" role="alert">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon icon-tabler icons-tabler-outline icon-tabler-check"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l5 5l10 -10" /></svg>
                <span class="font-medium">The classifier agrees.</span>
            </div>
        `);
    } else {
      $("#result").html(`
            <div class="flex items-center p-4 mb-4 text-red-800 border border-red-200 rounded-lg bg-red-50 gap-2" role="alert">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon icon-tabler icons-tabler-outline icon-tabler-x"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M18 6l-12 12" /><path d="M6 6l12 12" /></svg>
                <span class="font-medium">The classifier disagrees.</span>
            </div>
        `);
    }

    $("#result").removeClass("hidden");
  }

  function handleError(e) {
    cleanResult();

    $("#result").html(`
        <div class="flex items-center p-4 mb-4 text-yellow-800 border border-yellow-300 rounded-lg bg-yellow-50 gap-2" role="alert">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon icon-tabler icons-tabler-outline icon-tabler-bug"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M9 9v-1a3 3 0 0 1 6 0v1" /><path d="M8 9h8a6 6 0 0 1 1 3v3a5 5 0 0 1 -10 0v-3a6 6 0 0 1 1 -3" /><path d="M3 13l4 0" /><path d="M17 13l4 0" /><path d="M12 20l0 -6" /><path d="M4 19l3.35 -2" /><path d="M20 19l-3.35 -2" /><path d="M4 7l3.75 2.4" /><path d="M20 7l-3.75 2.4" /></svg>
            <span class="font-medium">An error occurred (see server log).</span>
        </div>
    `);

    $("#result").removeClass("hidden");
  }

  function startLoading() {
    $("#checkButton")
      .prop("disabled", true)
      .addClass("opacity-70 cursor-not-allowed");
    $("#checkButtonText").addClass("hidden");
    $("#checkSpinner").removeClass("hidden");
  }

  function stopLoading() {
    $("#checkButton")
      .prop("disabled", false)
      .removeClass("opacity-70 cursor-not-allowed");
    $("#checkSpinner").addClass("hidden");
    $("#checkButtonText").removeClass("hidden");
  }

  $("#smsInput").on("keypress", function (e) {
    $("#result").addClass("hidden");
  });

  $("input").click(function (e) {
    $("#result").addClass("hidden");
  });
});
