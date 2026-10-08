public interface BoardStateProvider{
    int getPlayer1Position();

    int getPlayer2Position();

    boolean isShowingWinner();

    boolean isWinnerPlayer1();
}
